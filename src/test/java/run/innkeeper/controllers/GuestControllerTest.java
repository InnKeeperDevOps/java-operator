package run.innkeeper.controllers;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.events.actions.guests.*;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.build.crd.BuildSpec;
import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.deployment.crd.DeploymentSpec;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.guest.crd.GuestStatus;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.service.crd.Service;
import run.innkeeper.v1.service.crd.ServiceSpec;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionSpec;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionStatus;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GuestControllerTest {

    @Mock
    K8sService k8sService;

    GuestController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new GuestController();
        Field field = GuestController.class.getDeclaredField("k8sService");
        field.setAccessible(true);
        field.set(controller, k8sService);
    }

    private Guest createGuest() {
        Guest guest = new Guest();
        guest.setMetaData("test-ns", "test-guest");
        GuestStatus status = new GuestStatus();
        status.setBuildChangeHistory(new ArrayList<>());
        status.setDeploymentChangeHistory(new ArrayList<>());
        status.setServicesChangeHistory(new ArrayList<>());
        status.setExtensionsChangeHistory(new ArrayList<>());
        status.setIngressChangeHistory(new ArrayList<>());
        guest.setStatus(status);
        return guest;
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkIfBuildUpdated_newBuild_createsBuildCRD() throws Exception {
        Guest guest = createGuest();
        BuildSettings bs = new BuildSettings();
        bs.setName("new-build");

        when(k8sService.getBuildByNamespaceAndName(any(Build.class))).thenReturn(null);

        controller.checkIfBuildUpdated(new CheckGuestBuildChanges(guest, bs));

        verify(k8sService).createBuild(any(Build.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkIfDeploymentUpdated_newDeployment_createsDeploymentCRD() throws Exception {
        Guest guest = createGuest();
        DeploymentSettings ds = new DeploymentSettings();
        ds.setName("new-dep");

        when(k8sService.getDeploymentByNamespaceAndName(any(Deployment.class))).thenReturn(null);

        controller.checkIfDeploymentUpdated(new CheckGuestDeploymentChanges(guest, ds));

        verify(k8sService).createDeployment(any(Deployment.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkGuestServiceChanges_newService_createsServiceCRD() throws Exception {
        Guest guest = createGuest();
        ServiceSettings ss = new ServiceSettings();
        ss.setName("new-svc");

        MixedOperation svcClient = mock(MixedOperation.class);
        when(k8sService.getServiceClient()).thenReturn(svcClient);
        Resource resource = mock(Resource.class);
        when(svcClient.resource(any(Service.class))).thenReturn(resource);
        when(resource.get()).thenReturn(null);
        when(resource.create()).thenReturn(new Service());

        controller.checkGuestServiceChanges(new CheckGuestServiceChanges(guest, ss));

        verify(resource).create();
    }

    @SuppressWarnings("unchecked")
    @Test
    void extensionChanges_newExtension_createsExtension() throws Exception {
        Guest guest = createGuest();
        SimpleExtensionSpec ses = new SimpleExtensionSpec();
        ses.setName("new-ext");
        ses.setType("BackendProxy");

        when(k8sService.getSimpleExtension(any(SimpleExtension.class))).thenReturn(null);
        when(k8sService.createSimpleExtension(any(SimpleExtension.class))).thenReturn(new SimpleExtension());

        controller.extensionChanges(new CheckGuestExtensionChanges(guest, ses));

        verify(k8sService).createSimpleExtension(any(SimpleExtension.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void deleteGuestBuild_deletesBuild() {
        Guest guest = createGuest();
        BuildSettings bs = new BuildSettings();
        bs.setName("build1");

        MixedOperation buildClient = mock(MixedOperation.class);
        when(k8sService.getBuildClient()).thenReturn(buildClient);
        Resource resource = mock(Resource.class);
        when(buildClient.resource(any(Build.class))).thenReturn(resource);

        controller.deleteGuestBuild(new DeleteGuestBuild(guest, bs));

        verify(resource).delete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void deleteGuestDeployment_deletesDeployment() {
        Guest guest = createGuest();
        DeploymentSettings ds = new DeploymentSettings();
        ds.setName("dep1");

        MixedOperation depClient = mock(MixedOperation.class);
        when(k8sService.getDeploymentClient()).thenReturn(depClient);
        Resource resource = mock(Resource.class);
        when(depClient.resource(any(Deployment.class))).thenReturn(resource);

        controller.deleteGuestDeployment(new DeleteGuestDeployment(guest, ds));

        verify(resource).delete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void deleteGuestService_deletesService() {
        Guest guest = createGuest();
        ServiceSettings ss = new ServiceSettings();
        ss.setName("svc1");

        MixedOperation svcClient = mock(MixedOperation.class);
        when(k8sService.getServiceClient()).thenReturn(svcClient);
        Resource resource = mock(Resource.class);
        when(svcClient.resource(any(Service.class))).thenReturn(resource);

        controller.deleteGuestService(new DeleteGuestService(guest, ss));

        verify(resource).delete();
    }

    @Test
    void deleteSimpleExtension_deletesExtension() {
        Guest guest = createGuest();
        SimpleExtensionSpec ses = new SimpleExtensionSpec();
        ses.setName("ext1");

        controller.deleteSimpleExtension(new DeleteGuestExtension(guest, ses));

        verify(k8sService).deleteSimpleExtension(any(SimpleExtension.class));
    }
}
