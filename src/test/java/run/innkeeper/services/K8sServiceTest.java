package run.innkeeper.services;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.v1.account.crd.Account;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.service.crd.Service;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class K8sServiceTest {

    @Mock
    KubernetesClient client;

    K8sService k8sService;

    @BeforeEach
    void setUp() throws Exception {
        k8sService = new K8sService();
        Field clientField = K8sService.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(k8sService, client);
    }

    @Test
    void get_returnsSingleton() {
        assertNotNull(K8sService.get());
    }

    @Test
    void getClient_returnsClient() {
        assertEquals(client, k8sService.getClient());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAllNamespaces_returnsNamespaces() {
        NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> nsOp = mock(NonNamespaceOperation.class);
        NamespaceList nsList = mock(NamespaceList.class);
        Namespace ns = new Namespace();
        ns.setMetadata(new ObjectMetaBuilder().withName("default").build());
        when(nsList.getItems()).thenReturn(Arrays.asList(ns));
        when(nsOp.list()).thenReturn(nsList);
        when(client.namespaces()).thenReturn(nsOp);

        List<Namespace> result = k8sService.getAllNamespaces();
        assertEquals(1, result.size());
        assertEquals("default", result.get(0).getMetadata().getName());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAllCRDs_returnsCRDs() {
        var apiExtensions = mock(io.fabric8.kubernetes.client.dsl.ApiextensionsAPIGroupDSL.class);
        var v1 = mock(io.fabric8.kubernetes.client.V1ApiextensionAPIGroupDSL.class);
        NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> crdOp = mock(NonNamespaceOperation.class);
        CustomResourceDefinitionList crdList = mock(CustomResourceDefinitionList.class);
        when(crdList.getItems()).thenReturn(Collections.emptyList());
        when(crdOp.list()).thenReturn(crdList);
        when(v1.customResourceDefinitions()).thenReturn(crdOp);
        when(apiExtensions.v1()).thenReturn(v1);
        when(client.apiextensions()).thenReturn(apiExtensions);

        List<CustomResourceDefinition> result = k8sService.getAllCRDs();
        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    void createJob_callsClientCreate() {
        Job job = new JobBuilder().withNewMetadata().withName("test").withNamespace("ns").endMetadata().build();
        NamespaceableResource<Job> resource = mock(NamespaceableResource.class);
        when(client.resource(job)).thenReturn(resource);

        k8sService.createJob(job);
        verify(resource).create();
    }

    @SuppressWarnings("unchecked")
    @Test
    void deleteJob_callsClientDelete() {
        Job job = new JobBuilder().withNewMetadata().withName("test").withNamespace("ns").endMetadata().build();
        NamespaceableResource<Job> resource = mock(NamespaceableResource.class);
        when(client.resource(job)).thenReturn(resource);

        k8sService.deleteJob(job);
        verify(resource).delete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void getJob_callsClientGet() {
        Job job = new JobBuilder().withNewMetadata().withName("test").withNamespace("ns").endMetadata().build();
        NamespaceableResource<Job> resource = mock(NamespaceableResource.class);
        when(client.resource(job)).thenReturn(resource);
        when(resource.get()).thenReturn(job);

        Job result = k8sService.getJob(job);
        assertEquals(job, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void logs_returnsLogs_whenJobAndPodExist() {
        Job job = new JobBuilder().withNewMetadata().withName("test-job").withNamespace("ns").endMetadata().build();
        NamespaceableResource<Job> jobResource = mock(NamespaceableResource.class);
        when(client.resource(job)).thenReturn(jobResource);
        when(jobResource.get()).thenReturn(job);

        // mock pods - single nsOp used for both listing and log retrieval
        MixedOperation<Pod, PodList, PodResource> podOps = mock(MixedOperation.class);
        when(client.pods()).thenReturn(podOps);
        var nsOp = mock(MixedOperation.class);
        when(podOps.inNamespace("ns")).thenReturn(nsOp);

        // mock pod listing
        var labelOp = mock(MixedOperation.class);
        when(nsOp.withLabel("job-name", "test-job")).thenReturn(labelOp);
        Pod pod = new PodBuilder()
            .withNewMetadata().withName("test-pod").withNamespace("ns").endMetadata()
            .withNewSpec()
            .addNewContainer().withName("container1").endContainer()
            .endSpec()
            .build();
        PodList podList = mock(PodList.class);
        when(podList.getItems()).thenReturn(Arrays.asList(pod));
        when(labelOp.list()).thenReturn(podList);

        // mock log retrieval
        var nameOp = mock(PodResource.class);
        when(nsOp.withName("test-pod")).thenReturn(nameOp);
        when(nameOp.inContainer("container1")).thenReturn(nameOp);
        when(nameOp.getLog()).thenReturn("build log output");

        String result = k8sService.logs(job);
        assertEquals("build log output", result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void logs_returnsNull_whenJobNotExists() {
        Job job = new JobBuilder().withNewMetadata().withName("test-job").withNamespace("ns").endMetadata().build();
        NamespaceableResource<Job> jobResource = mock(NamespaceableResource.class);
        when(client.resource(job)).thenReturn(jobResource);
        when(jobResource.get()).thenReturn(null);

        String result = k8sService.logs(job);
        assertNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void logStream_returnsNull_whenJobNotExists() {
        Job job = new JobBuilder().withNewMetadata().withName("test-job").withNamespace("ns").endMetadata().build();
        NamespaceableResource<Job> jobResource = mock(NamespaceableResource.class);
        when(client.resource(job)).thenReturn(jobResource);
        when(jobResource.get()).thenReturn(null);

        InputStream result = k8sService.logStream(job);
        assertNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void logWatch_returnsNull_whenJobNotExists() {
        Job job = new JobBuilder().withNewMetadata().withName("test-job").withNamespace("ns").endMetadata().build();
        NamespaceableResource<Job> jobResource = mock(NamespaceableResource.class);
        when(client.resource(job)).thenReturn(jobResource);
        when(jobResource.get()).thenReturn(null);

        var result = k8sService.logWatch(job);
        assertNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getGuestClient_returnsClient() {
        MixedOperation guestOp = mock(MixedOperation.class);
        when(client.resources(Guest.class)).thenReturn(guestOp);
        assertNotNull(k8sService.getGuestClient());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getBuildClient_returnsClient() {
        MixedOperation buildOp = mock(MixedOperation.class);
        when(client.resources(Build.class)).thenReturn(buildOp);
        assertNotNull(k8sService.getBuildClient());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getDeploymentClient_returnsClient() {
        MixedOperation depOp = mock(MixedOperation.class);
        when(client.resources(Deployment.class)).thenReturn(depOp);
        assertNotNull(k8sService.getDeploymentClient());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getServiceClient_returnsClient() {
        MixedOperation svcOp = mock(MixedOperation.class);
        when(client.resources(Service.class)).thenReturn(svcOp);
        assertNotNull(k8sService.getServiceClient());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAccountClient_returnsClient() {
        MixedOperation acctOp = mock(MixedOperation.class);
        when(client.resources(Account.class)).thenReturn(acctOp);
        assertNotNull(k8sService.getAccountClient());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getSimpleExtensionClient_returnsClient() {
        MixedOperation seOp = mock(MixedOperation.class);
        when(client.resources(SimpleExtension.class)).thenReturn(seOp);
        assertNotNull(k8sService.getSimpleExtensionClient());
    }

    @SuppressWarnings("unchecked")
    @Test
    void createBuild_callsCreate() {
        Build build = new Build();
        build.setMetaData("ns1", "build1");

        MixedOperation buildOp = mock(MixedOperation.class);
        when(client.resources(Build.class)).thenReturn(buildOp);
        var nsOp = mock(MixedOperation.class);
        when(buildOp.inNamespace("ns1")).thenReturn(nsOp);
        Resource resource = mock(Resource.class);
        when(nsOp.resource(build)).thenReturn(resource);

        k8sService.createBuild(build);
        verify(resource).create();
    }

    @SuppressWarnings("unchecked")
    @Test
    void createDeployment_callsCreate() {
        Deployment dep = new Deployment();
        dep.setMetaData("ns1", "dep1");

        MixedOperation depOp = mock(MixedOperation.class);
        when(client.resources(Deployment.class)).thenReturn(depOp);
        var nsOp = mock(MixedOperation.class);
        when(depOp.inNamespace("ns1")).thenReturn(nsOp);
        Resource resource = mock(Resource.class);
        when(nsOp.resource(dep)).thenReturn(resource);

        k8sService.createDeployment(dep);
        verify(resource).create();
    }

    @SuppressWarnings("unchecked")
    @Test
    void getBuildByNamespaceAndName_withStrings() {
        MixedOperation buildOp = mock(MixedOperation.class);
        when(client.resources(Build.class)).thenReturn(buildOp);
        var nsOp = mock(MixedOperation.class);
        when(buildOp.inNamespace("ns1")).thenReturn(nsOp);
        Resource resource = mock(Resource.class);
        when(nsOp.resource(any(Build.class))).thenReturn(resource);
        when(resource.get()).thenReturn(null);

        Build result = k8sService.getBuildByNamespaceAndName("ns1", "build1");
        assertNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getDeploymentByNamespaceAndName_withStrings() {
        MixedOperation depOp = mock(MixedOperation.class);
        when(client.resources(Deployment.class)).thenReturn(depOp);
        var nsOp = mock(MixedOperation.class);
        when(depOp.inNamespace("ns1")).thenReturn(nsOp);
        Resource resource = mock(Resource.class);
        when(nsOp.resource(any(Deployment.class))).thenReturn(resource);
        when(resource.get()).thenReturn(null);

        Deployment result = k8sService.getDeploymentByNamespaceAndName("ns1", "dep1");
        assertNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getGuestByNameAndNamespace_returnsGuest() {
        MixedOperation guestOp = mock(MixedOperation.class);
        when(client.resources(Guest.class)).thenReturn(guestOp);
        var nsOp = mock(MixedOperation.class);
        when(guestOp.inNamespace("ns1")).thenReturn(nsOp);
        Resource resource = mock(Resource.class);
        when(nsOp.withName("guest1")).thenReturn(resource);
        Guest guest = new Guest();
        when(resource.item()).thenReturn(guest);

        Guest result = k8sService.getGuestByNameAndNamespace("guest1", "ns1");
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getSimpleExtension_callsGet() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("ns", "ext1");

        MixedOperation seOp = mock(MixedOperation.class);
        when(client.resources(SimpleExtension.class)).thenReturn(seOp);
        Resource resource = mock(Resource.class);
        when(seOp.resource(se)).thenReturn(resource);
        when(resource.get()).thenReturn(se);

        SimpleExtension result = k8sService.getSimpleExtension(se);
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void createSimpleExtension_callsCreate() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("ns", "ext1");

        MixedOperation seOp = mock(MixedOperation.class);
        when(client.resources(SimpleExtension.class)).thenReturn(seOp);
        Resource resource = mock(Resource.class);
        when(seOp.resource(se)).thenReturn(resource);
        when(resource.create()).thenReturn(se);

        SimpleExtension result = k8sService.createSimpleExtension(se);
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void updateSimpleExtension_callsPatch() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("ns", "ext1");

        MixedOperation seOp = mock(MixedOperation.class);
        when(client.resources(SimpleExtension.class)).thenReturn(seOp);
        Resource resource = mock(Resource.class);
        when(seOp.resource(se)).thenReturn(resource);
        when(resource.patch()).thenReturn(se);

        SimpleExtension result = k8sService.updateSimpleExtension(se);
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void updateSimpleExtensionStatus_callsPatchStatus() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("ns", "ext1");

        MixedOperation seOp = mock(MixedOperation.class);
        when(client.resources(SimpleExtension.class)).thenReturn(seOp);
        Resource resource = mock(Resource.class);
        when(seOp.resource(se)).thenReturn(resource);
        when(resource.patchStatus()).thenReturn(se);

        SimpleExtension result = k8sService.updateSimpleExtensionStatus(se);
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void deleteSimpleExtension_callsDelete() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("ns", "ext1");

        MixedOperation seOp = mock(MixedOperation.class);
        when(client.resources(SimpleExtension.class)).thenReturn(seOp);
        Resource resource = mock(Resource.class);
        when(seOp.resource(se)).thenReturn(resource);

        k8sService.deleteSimpleExtension(se);
        verify(resource).delete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void logs_returnsNull_whenNoPods() {
        Job job = new JobBuilder().withNewMetadata().withName("test-job").withNamespace("ns").endMetadata().build();
        NamespaceableResource<Job> jobResource = mock(NamespaceableResource.class);
        when(client.resource(job)).thenReturn(jobResource);
        when(jobResource.get()).thenReturn(job);

        MixedOperation<Pod, PodList, PodResource> podOps = mock(MixedOperation.class);
        when(client.pods()).thenReturn(podOps);
        var nsOp = mock(MixedOperation.class);
        when(podOps.inNamespace("ns")).thenReturn(nsOp);
        var labelOp = mock(MixedOperation.class);
        when(nsOp.withLabel("job-name", "test-job")).thenReturn(labelOp);
        PodList podList = mock(PodList.class);
        when(podList.getItems()).thenReturn(Collections.emptyList());
        when(labelOp.list()).thenReturn(podList);

        assertNull(k8sService.logs(job));
    }

    @SuppressWarnings("unchecked")
    @Test
    void logStream_returnsNull_whenNoPods() {
        Job job = new JobBuilder().withNewMetadata().withName("test-job").withNamespace("ns").endMetadata().build();
        NamespaceableResource<Job> jobResource = mock(NamespaceableResource.class);
        when(client.resource(job)).thenReturn(jobResource);
        when(jobResource.get()).thenReturn(job);

        MixedOperation<Pod, PodList, PodResource> podOps = mock(MixedOperation.class);
        when(client.pods()).thenReturn(podOps);
        var nsOp = mock(MixedOperation.class);
        when(podOps.inNamespace("ns")).thenReturn(nsOp);
        var labelOp = mock(MixedOperation.class);
        when(nsOp.withLabel("job-name", "test-job")).thenReturn(labelOp);
        PodList podList = mock(PodList.class);
        when(podList.getItems()).thenReturn(Collections.emptyList());
        when(labelOp.list()).thenReturn(podList);

        assertNull(k8sService.logStream(job));
    }

    @SuppressWarnings("unchecked")
    @Test
    void logWatch_returnsNull_whenNoPods() {
        Job job = new JobBuilder().withNewMetadata().withName("test-job").withNamespace("ns").endMetadata().build();
        NamespaceableResource<Job> jobResource = mock(NamespaceableResource.class);
        when(client.resource(job)).thenReturn(jobResource);
        when(jobResource.get()).thenReturn(job);

        MixedOperation<Pod, PodList, PodResource> podOps = mock(MixedOperation.class);
        when(client.pods()).thenReturn(podOps);
        var nsOp = mock(MixedOperation.class);
        when(podOps.inNamespace("ns")).thenReturn(nsOp);
        var labelOp = mock(MixedOperation.class);
        when(nsOp.withLabel("job-name", "test-job")).thenReturn(labelOp);
        PodList podList = mock(PodList.class);
        when(podList.getItems()).thenReturn(Collections.emptyList());
        when(labelOp.list()).thenReturn(podList);

        assertNull(k8sService.logWatch(job));
    }

    @Test
    void crdObject_constructorAndFields() {
        K8sService.CRDObject crdObj = new K8sService.CRDObject(Guest.class, "path/to/file.yml");
        assertEquals(Guest.class, crdObj.clazz);
        assertEquals("path/to/file.yml", crdObj.file);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getBuildByNamespaceAndName_withBuildObject() {
        Build build = new Build();
        build.setMetaData("ns1", "build1");

        MixedOperation buildOp = mock(MixedOperation.class);
        when(client.resources(Build.class)).thenReturn(buildOp);
        var nsOp = mock(MixedOperation.class);
        when(buildOp.inNamespace("ns1")).thenReturn(nsOp);
        Resource resource = mock(Resource.class);
        when(nsOp.resource(build)).thenReturn(resource);
        when(resource.get()).thenReturn(build);

        Build result = k8sService.getBuildByNamespaceAndName(build);
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getDeploymentByNamespaceAndName_withDeploymentObject() {
        Deployment dep = new Deployment();
        dep.setMetaData("ns1", "dep1");

        MixedOperation depOp = mock(MixedOperation.class);
        when(client.resources(Deployment.class)).thenReturn(depOp);
        var nsOp = mock(MixedOperation.class);
        when(depOp.inNamespace("ns1")).thenReturn(nsOp);
        Resource resource = mock(Resource.class);
        when(nsOp.resource(dep)).thenReturn(resource);
        when(resource.get()).thenReturn(dep);

        Deployment result = k8sService.getDeploymentByNamespaceAndName(dep);
        assertNotNull(result);
    }
}
