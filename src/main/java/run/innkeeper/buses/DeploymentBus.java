package run.innkeeper.buses;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.build.crd.BuiltContainer;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;
import run.innkeeper.v1.guest.crd.objects.build.Publish;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class DeploymentBus {
    public static DeploymentBus deploymentBus = new DeploymentBus();
    K8sService k8sService = K8sService.get();
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

    public static DeploymentBus get() {
        return deploymentBus;
    }

    public String getImage(Build build) {
        List<BuiltContainer> builtContainers = build.getStatus().getCompleted();
        Publish publish = build.getSpec().getBuildSettings().getPublish();
        return publish.getRegistry() + "/" + publish.getTag() + ":" + builtContainers.get(builtContainers.size() - 1).getGitSource().getCommit();
    }

    public Container getContainer(run.innkeeper.v1.guest.crd.objects.deployment.Container containerSettings, Build build) {
        Container container = new Container();
        container.setName(containerSettings.getName());
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
                createPullSecret(secret.getData().get("username"), secret.getData().get("password"), host, deploymentSettings.getNamespace());
            }
        }
    }

    public void createPullSecret(String username, String password, String host, String namespace) {
        String base64Auths = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        String dockerAuth = "{\"auths\":{\"" + host + "\":{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"auth\":\"" + base64Auths + "\"}}}";
        Map<String, String> data = new HashMap<>();
        data.put(".dockerconfigjson", dockerAuth);
        Secret secret = new SecretBuilder()
            .withNewMetadata()
            .withName("docker-pull-" + host)
            .withNamespace(namespace)
            .endMetadata()
            .withType("kubernetes.io/dockerconfigjson")
            .withData(data)
            .build();
        k8sService.getClient().secrets().resource(secret).create();
    }

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

    public Deployment createDeployment(DeploymentSettings deploymentSettings, Map<String, Build> builds) {
        Deployment deployment = buildDeployment(deploymentSettings, builds);
        return k8sService.getClient().resource(deployment).create();
    }

    public Deployment updateDeployment(DeploymentSettings deploymentSettings, Map<String, Build> builds) {
        Deployment deployment = buildDeployment(deploymentSettings, builds);
        return k8sService.getClient().resource(deployment).patch();
    }

    private Deployment buildDeployment(DeploymentSettings deploymentSettings, Map<String, Build> builds) {
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
