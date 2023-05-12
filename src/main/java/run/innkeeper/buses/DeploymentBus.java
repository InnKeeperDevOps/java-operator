package run.innkeeper.buses;


import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonObject;
import org.reflections.ReflectionUtils;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.build.crd.BuiltContainer;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;
import run.innkeeper.v1.guest.crd.objects.build.Publish;

/**
 * The type Deployment bus.
 */
public class DeploymentBus {
  /**
   * The constant deploymentBus.
   */
  public static DeploymentBus deploymentBus = new DeploymentBus();
  /**
   * The K 8 s service.
   */
  K8sService k8sService = K8sService.get();
  /**
   * The Container getter methods.
   */
  Map<String, Method> containerGetterMethods = ReflectionUtils.getAllMethods(
          Container.class,
          ReflectionUtils.withModifier(Modifier.PUBLIC),
          ReflectionUtils.withPrefix("get")
      )
      .stream()
      .filter(method -> !method.getName().equals("getImage"))
      .reduce(
          new HashMap<>(),
          (list, b) -> {
            list.put(b.getName().substring(3), b);
            return list;
          },
          (list, b) -> list
      );
  /**
   * The Container setter methods.
   */
  Map<String, Method> containerSetterMethods = ReflectionUtils.getAllMethods(
          Container.class,
          ReflectionUtils.withModifier(Modifier.PUBLIC),
          ReflectionUtils.withPrefix("set")
      )
      .stream()
      .filter(method -> !method.getName().equals("setImage"))
      .reduce(
          new HashMap<>(),
          (list, b) -> {
            list.put(b.getName().substring(3), b);
            return list;
          },
          (list, b) -> list
      );

  /**
   * Get deployment bus.
   *
   * @return the deployment bus
   */
  public static DeploymentBus get() {
    return deploymentBus;
  }

  /**
   * Gets image.
   *
   * @param build the build
   * @return the image
   */
  public String getImage(Build build) {
    if (build.getStatus() != null) {
      List<BuiltContainer> builtContainers = build.getStatus().getCompleted();
      Publish publish = build.getSpec().getBuildSettings().getPublish();
      return publish.getRegistry() + "/" + publish.getTag() + ":" + builtContainers.get(builtContainers.size() - 1).getGitSource().getCommit();
    }
    return null;
  }

  /**
   * Gets container.
   *
   * @param containerSettings the container settings
   * @param build             the build
   * @return the container
   */
  public Container getContainer(run.innkeeper.v1.guest.crd.objects.deployment.Container containerSettings, Build build) {
    Container container = new Container();
    container.setName(containerSettings.getName());
    container.setImagePullPolicy("Always");
    container.setImage(getImage(build));
    containerGetterMethods.forEach((key, method) -> {
      try {
        Object obj = method.invoke(containerSettings);
        if (obj != null) {
          Method setterMethod = containerSetterMethods.get(key);
          if (setterMethod != null) {
            setterMethod.invoke(container, obj);
          }
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    });
    return container;
  }

  /**
   * Create pull secret if not exist.
   *
   * @param deploymentSettings the deployment settings
   * @param build              the build
   */
  public void createPullSecretIfNotExist(DeploymentSettings deploymentSettings, Build build) {
    String host = build.getSpec().getBuildSettings().getPublish().getRegistry().toLowerCase();
    String name = "docker-pull-" + host;
    Secret secret = new SecretBuilder()
        .withNewMetadata()
        .withName(name)
        .withNamespace(deploymentSettings.getNamespace())
        .endMetadata()
        .build();
    if (k8sService.getClient().secrets().resource(secret).get() == null) {
      secret = new SecretBuilder()
          .withNewMetadata()
          .withName(build.getSpec().getBuildSettings().getPublish().getSecret())
          .withNamespace(build.getSpec().getBuildSettings().getNamespace())
          .endMetadata()
          .build();
      secret = k8sService.getClient().secrets().resource(secret).get();
      if (secret != null) {
        createPullSecret(
            base64Decode(secret.getData().get("username")),
            base64Decode(secret.getData().get("email")),
            base64Decode(secret.getData().get("password")),
            base64Decode(secret.getData().get("server")),
            deploymentSettings.getNamespace()
        );
      }
    }
  }

  /**
   * Base 64 decode string.
   *
   * @param encoded the encoded
   * @return the string
   */
  public static String base64Decode(String encoded) {
    return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
  }

  /**
   * Create pull secret.
   *
   * @param username  the username
   * @param email     the email
   * @param password  the password
   * @param host      the host
   * @param namespace the namespace
   */
  public void createPullSecret(
      String username,
      String email,
      String password,
      String host,
      String namespace
  ) {
    Secret secret = new SecretBuilder()
        .withNewMetadata()
        .withName("docker-pull-" + host)
        .withNamespace(namespace)
        .endMetadata()
        .withType("kubernetes.io/dockerconfigjson")
        .withData(createDockerConfigJson(host, username, email, password))
        .build();
    k8sService.getClient().secrets().resource(secret).create();
  }

  private static Map<String, String> createDockerConfigJson(String dockerServer, String username, String email, String password) {
    String auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    Map<String, String> data = new HashMap<>();
    data.put(".dockerconfigjson", Base64.getEncoder().encodeToString(createDockerConfigJsonString(dockerServer, username, email, password, auth).getBytes(StandardCharsets.UTF_8)));
    return data;
  }

  private static String createDockerConfigJsonString(String dockerServer, String username, String email, String password, String auth) {
    JsonObject authJsonObject = Json.createObjectBuilder()
        .add("username", username)
        .add("password", password)
        .add("email", email)
        .add("auth", auth)
        .build();
    JsonObject authsJsonObject = Json.createObjectBuilder()
        .add(dockerServer, authJsonObject)
        .build();
    JsonObject jsonObject = Json.createObjectBuilder()
        .add("auths", authsJsonObject)
        .build();
    return jsonObject.toString();
  }

  /**
   * Gets pull secret.
   *
   * @param deploymentSettings the deployment settings
   * @param builds             the builds
   * @return the pull secret
   */
  public List<LocalObjectReference> getPullSecret(DeploymentSettings deploymentSettings, Map<String, Build> builds) {
    List<LocalObjectReference> secretReferences = new ArrayList<>();
    for (Build build : builds.values()) {
      String name = "docker-pull-" + build.getSpec().getBuildSettings().getPublish().getRegistry().toLowerCase();
      if (secretReferences.stream().filter(secretReference -> secretReference.getName().equals(name)).count() == 0) {
        createPullSecretIfNotExist(deploymentSettings, build);
        LocalObjectReference pullSecretReference = new LocalObjectReference();
        pullSecretReference.setName(name);
        secretReferences.add(pullSecretReference);
      }
    }
    return secretReferences;
  }

  /**
   * Create deployment deployment.
   *
   * @param deploymentSettings the deployment settings
   * @param builds             the builds
   * @return the deployment
   */
  public Deployment createDeployment(DeploymentSettings deploymentSettings, Map<String, Build> builds) {
    Deployment deployment = buildDeployment(deploymentSettings, builds);
    return k8sService.getClient().resource(deployment).create();
  }

  /**
   * Get deployment.
   *
   * @param deploymentSettings the deployment settings
   * @return the deployment
   */
  public Deployment get(DeploymentSettings deploymentSettings) {
    Deployment deployment = new DeploymentBuilder()
        .withNewMetadata()
        .withName(deploymentSettings.getName())
        .withNamespace(deploymentSettings.getNamespace())
        .endMetadata().build();
    return k8sService.getClient().resource(deployment).get();
  }

  /**
   * Update deployment deployment.
   *
   * @param deploymentSettings the deployment settings
   * @param builds             the builds
   * @return the deployment
   */
  public Deployment updateDeployment(DeploymentSettings deploymentSettings, Map<String, Build> builds) {
    Deployment deployment = buildDeployment(deploymentSettings, builds);
    return k8sService.getClient().resource(deployment).patch();
  }

  /**
   * Update deployment deployment.
   *
   * @param deployment the deployment
   * @return the deployment
   */
  public Deployment updateDeployment(Deployment deployment) {
    return k8sService.getClient().resource(deployment).patch();
  }

  /**
   * Delete deployment.
   *
   * @param deploymentSettings the deployment settings
   */
  public void deleteDeployment(DeploymentSettings deploymentSettings) {
    k8sService
        .getClient()
        .apps()
        .deployments()
        .resource(
            new DeploymentBuilder()
                .withNewMetadata()
                .withName(deploymentSettings.getName())
                .withNamespace(deploymentSettings.getNamespace())
                .endMetadata()
                .build()
        )
        .delete();
  }

  /**
   * Build deployment deployment.
   *
   * @param deploymentSettings the deployment settings
   * @param builds             the builds
   * @return the deployment
   */
  public Deployment buildDeployment(DeploymentSettings deploymentSettings, Map<String, Build> builds) {
    Map<String, String> labels = new HashMap<>();
    labels.put("app-selector", deploymentSettings.getName());
    return new DeploymentBuilder()
        .withNewMetadata()
        .withName(deploymentSettings.getName())
        .withNamespace(deploymentSettings.getNamespace())
        .withLabels(labels)
        .endMetadata()
        .withNewSpec()
        .withReplicas(deploymentSettings.getReplicas())
        .withNewSelector()
        .addToMatchLabels(labels)
        .endSelector()
        .withNewTemplate()
        .withNewMetadata()
        .withName(deploymentSettings.getName())
        .withNamespace(deploymentSettings.getNamespace())
        .withLabels(labels)
        .endMetadata()
        .withNewSpec()
        .withImagePullSecrets(getPullSecret(deploymentSettings, builds))
        .withVolumes(deploymentSettings.getVolumes())
        .withContainers(
            deploymentSettings
                .getContainers()
                .stream()
                .map(container -> getContainer(container, builds.get(container.getBuildName())))
                .collect(Collectors.toList())
        )
        .endSpec()
        .endTemplate()
        .endSpec()
        .build();
  }
}
