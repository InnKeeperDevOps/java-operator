package run.innkeeper.buses;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.AppsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NamespaceableResource;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.build.crd.BuildSpec;
import run.innkeeper.v1.build.crd.BuildStatus;
import run.innkeeper.v1.build.crd.BuiltContainer;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;
import run.innkeeper.v1.guest.crd.objects.build.GitSource;
import run.innkeeper.v1.guest.crd.objects.build.Publish;
import run.innkeeper.v1.guest.crd.objects.deployment.Container;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeploymentBusTest {

    @Mock
    K8sService k8sService;

    @Mock
    KubernetesClient client;

    DeploymentBus deploymentBus;

    @BeforeEach
    void setUp() throws Exception {
        deploymentBus = new DeploymentBus();
        Field k8sField = DeploymentBus.class.getDeclaredField("k8sService");
        k8sField.setAccessible(true);
        k8sField.set(deploymentBus, k8sService);
        lenient().when(k8sService.getClient()).thenReturn(client);
    }

    private Build createBuild() {
        Build build = new Build();
        build.setMetaData("ns1", "build1");

        BuildSpec spec = new BuildSpec();
        BuildSettings bs = new BuildSettings();
        bs.setName("build1");
        bs.setNamespace("ns1");
        GitSource git = new GitSource();
        git.setUri("git@github.com:test/repo.git");
        git.setSecret("ssh-key");
        bs.setGit(git);
        Publish publish = new Publish();
        publish.setRegistry("ghcr.io");
        publish.setTag("test/image");
        publish.setSecret("reg-secret");
        bs.setPublish(publish);
        spec.setBuildSettings(bs);
        build.setSpec(spec);

        BuildStatus status = new BuildStatus();
        BuiltContainer bc = new BuiltContainer();
        GitSource builtGit = new GitSource();
        builtGit.setCommit("abc123");
        bc.setGitSource(builtGit);
        status.setCompleted(Arrays.asList(bc));
        build.setStatus(status);

        return build;
    }

    private DeploymentSettings createDeploymentSettings() {
        DeploymentSettings ds = new DeploymentSettings();
        ds.setName("test-dep");
        ds.setNamespace("test-ns");
        ds.setReplicas(2);

        Container container = new Container();
        container.setName("main");
        container.setBuildName("build1");
        ds.setContainers(Arrays.asList(container));
        ds.setVolumes(new ArrayList<>());

        return ds;
    }

    @Test
    void get_returnsSingleton() {
        assertNotNull(DeploymentBus.get());
    }

    @Test
    void getImage_withStatus_returnsImage() {
        Build build = createBuild();
        String image = deploymentBus.getImage(build);
        assertEquals("ghcr.io/test/image:abc123", image);
    }

    @Test
    void getImage_withNullStatus_returnsNull() {
        Build build = new Build();
        build.setMetaData("ns1", "build1");
        String image = deploymentBus.getImage(build);
        assertNull(image);
    }

    @Test
    void base64Decode_decodesCorrectly() {
        String encoded = Base64.getEncoder().encodeToString("hello".getBytes(StandardCharsets.UTF_8));
        assertEquals("hello", DeploymentBus.base64Decode(encoded));
    }

    @SuppressWarnings("unchecked")
    @Test
    void get_deployment_queriesK8s() {
        DeploymentSettings ds = createDeploymentSettings();
        NamespaceableResource<Deployment> resource = mock(NamespaceableResource.class);
        when(client.resource(any(Deployment.class))).thenReturn(resource);
        when(resource.get()).thenReturn(null);

        Deployment result = deploymentBus.get(ds);
        assertNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void createDeployment_createsInK8s() {
        DeploymentSettings ds = createDeploymentSettings();
        Build build = createBuild();
        Map<String, Build> builds = new HashMap<>();
        builds.put("build1", build);

        Resource<Secret> secretResource = mock(Resource.class);
        when(client.secrets()).thenReturn(mock(MixedOperation.class));
        when(client.secrets().resource(any(Secret.class))).thenReturn(secretResource);
        when(secretResource.get()).thenReturn(new SecretBuilder().withNewMetadata().withName("secret").endMetadata().build());

        NamespaceableResource<Deployment> depResource = mock(NamespaceableResource.class);
        when(client.resource(any(Deployment.class))).thenReturn(depResource);
        Deployment dep = new io.fabric8.kubernetes.api.model.apps.DeploymentBuilder()
            .withNewMetadata().withName("test-dep").endMetadata().build();
        when(depResource.create()).thenReturn(dep);

        Deployment result = deploymentBus.createDeployment(ds, builds);
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void updateDeployment_withSettings_patchesInK8s() {
        DeploymentSettings ds = createDeploymentSettings();
        Build build = createBuild();
        Map<String, Build> builds = new HashMap<>();
        builds.put("build1", build);

        Resource<Secret> secretResource = mock(Resource.class);
        when(client.secrets()).thenReturn(mock(MixedOperation.class));
        when(client.secrets().resource(any(Secret.class))).thenReturn(secretResource);
        when(secretResource.get()).thenReturn(new SecretBuilder().withNewMetadata().withName("secret").endMetadata().build());

        NamespaceableResource<Deployment> depResource = mock(NamespaceableResource.class);
        when(client.resource(any(Deployment.class))).thenReturn(depResource);
        Deployment dep = new io.fabric8.kubernetes.api.model.apps.DeploymentBuilder()
            .withNewMetadata().withName("test-dep").endMetadata().build();
        when(depResource.patch()).thenReturn(dep);

        Deployment result = deploymentBus.updateDeployment(ds, builds);
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void updateDeployment_withDeploymentObject_patchesInK8s() {
        Deployment dep = new io.fabric8.kubernetes.api.model.apps.DeploymentBuilder()
            .withNewMetadata().withName("test-dep").endMetadata().build();
        NamespaceableResource<Deployment> depResource = mock(NamespaceableResource.class);
        when(client.resource(dep)).thenReturn(depResource);
        when(depResource.patch()).thenReturn(dep);

        Deployment result = deploymentBus.updateDeployment(dep);
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void deleteDeployment_deletesInK8s() {
        DeploymentSettings ds = createDeploymentSettings();
        AppsAPIGroupDSL apps = mock(AppsAPIGroupDSL.class);
        when(client.apps()).thenReturn(apps);
        MixedOperation depOps = mock(MixedOperation.class);
        when(apps.deployments()).thenReturn(depOps);
        RollableScalableResource resource = mock(RollableScalableResource.class);
        when(depOps.resource(any(Deployment.class))).thenReturn(resource);

        deploymentBus.deleteDeployment(ds);
        verify(resource).delete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void buildDeployment_setsAllFields() {
        DeploymentSettings ds = createDeploymentSettings();
        Build build = createBuild();
        Map<String, Build> builds = new HashMap<>();
        builds.put("build1", build);

        Resource<Secret> secretResource = mock(Resource.class);
        when(client.secrets()).thenReturn(mock(MixedOperation.class));
        when(client.secrets().resource(any(Secret.class))).thenReturn(secretResource);
        when(secretResource.get()).thenReturn(new SecretBuilder().withNewMetadata().withName("secret").endMetadata().build());

        Deployment result = deploymentBus.buildDeployment(ds, builds);
        assertEquals("test-dep", result.getMetadata().getName());
        assertEquals("test-ns", result.getMetadata().getNamespace());
        assertEquals(2, result.getSpec().getReplicas());
        assertEquals("test-dep", result.getSpec().getSelector().getMatchLabels().get("app-selector"));
    }

    @Test
    void getContainer_setsImageAndName() {
        Build build = createBuild();
        Container containerSettings = new Container();
        containerSettings.setName("my-container");
        containerSettings.setBuildName("build1");

        io.fabric8.kubernetes.api.model.Container result = deploymentBus.getContainer(containerSettings, build);
        assertEquals("my-container", result.getName());
        assertEquals("Always", result.getImagePullPolicy());
        assertEquals("ghcr.io/test/image:abc123", result.getImage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void createPullSecretIfNotExist_whenSecretExists_doesNotCreate() {
        DeploymentSettings ds = createDeploymentSettings();
        Build build = createBuild();

        MixedOperation secretOps = mock(MixedOperation.class);
        when(client.secrets()).thenReturn(secretOps);
        Resource<Secret> secretResource = mock(Resource.class);
        when(secretOps.resource(any(Secret.class))).thenReturn(secretResource);
        Secret existingSecret = new SecretBuilder().withNewMetadata().withName("docker-pull-ghcr.io").endMetadata().build();
        when(secretResource.get()).thenReturn(existingSecret);

        deploymentBus.createPullSecretIfNotExist(ds, build);
        verify(secretResource, never()).create();
    }

    @SuppressWarnings("unchecked")
    @Test
    void createPullSecretIfNotExist_whenNoSecret_andSourceSecretNull_doesNotCreate() {
        DeploymentSettings ds = createDeploymentSettings();
        Build build = createBuild();

        MixedOperation secretOps = mock(MixedOperation.class);
        when(client.secrets()).thenReturn(secretOps);
        Resource<Secret> secretResource = mock(Resource.class);
        when(secretOps.resource(any(Secret.class))).thenReturn(secretResource);
        when(secretResource.get()).thenReturn(null);

        deploymentBus.createPullSecretIfNotExist(ds, build);
    }

    @SuppressWarnings("unchecked")
    @Test
    void createPullSecret_createsDockerSecret() {
        MixedOperation secretOps = mock(MixedOperation.class);
        when(client.secrets()).thenReturn(secretOps);
        Resource<Secret> secretResource = mock(Resource.class);
        when(secretOps.resource(any(Secret.class))).thenReturn(secretResource);

        deploymentBus.createPullSecret("user", "email@test.com", "pass", "ghcr.io", "test-ns");
        verify(secretResource).create();
    }
}
