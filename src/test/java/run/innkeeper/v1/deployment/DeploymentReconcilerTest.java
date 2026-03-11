package run.innkeeper.v1.deployment;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.buses.DeploymentBus;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.deployment.crd.*;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeploymentReconcilerTest {

    @Mock
    K8sService k8sService;

    @Mock
    DeploymentBus deploymentBus;

    @Mock
    Context<Deployment> context;

    DeploymentReconciler reconciler;

    @BeforeEach
    void setUp() throws Exception {
        reconciler = new DeploymentReconciler();
        Field k8sField = DeploymentReconciler.class.getDeclaredField("k8sService");
        k8sField.setAccessible(true);
        k8sField.set(reconciler, k8sService);
        Field depBusField = DeploymentReconciler.class.getDeclaredField("deploymentBus");
        depBusField.setAccessible(true);
        depBusField.set(reconciler, deploymentBus);
    }

    private Deployment createDeployment(DeploymentState state) {
        Deployment dep = new Deployment();
        dep.setMetaData("test-ns", "test-dep");
        DeploymentSpec spec = new DeploymentSpec();
        DeploymentSettings ds = new DeploymentSettings();
        ds.setName("test-dep");
        ds.setNamespace("test-ns");
        ds.setContainers(new ArrayList<>());
        spec.setDeploymentSettings(ds);
        dep.setSpec(spec);
        if (state != null) {
            dep.setStatus(new DeploymentStatus());
            dep.getStatus().setState(state);
        }
        return dep;
    }

    @Test
    void reconcile_nullStatus_initializesStatus() throws Exception {
        Deployment dep = createDeployment(null);

        UpdateControl<Deployment> result = reconciler.reconcile(dep, context);

        assertNotNull(dep.getStatus());
        assertEquals(DeploymentState.NEED_TO_DEPLOY, dep.getStatus().getState());
        assertTrue(result.isUpdateStatus());
    }

    @Test
    void reconcile_deployedState_doesNotThrow() throws Exception {
        Deployment dep = createDeployment(DeploymentState.DEPLOYED);
        assertDoesNotThrow(() -> reconciler.reconcile(dep, context));
    }

    @Test
    void reconcile_redeployState_doesNotThrow() throws Exception {
        Deployment dep = createDeployment(DeploymentState.REDEPLOY);
        assertDoesNotThrow(() -> reconciler.reconcile(dep, context));
    }

    @Test
    void reconcile_needToDeployState_doesNotThrow() throws Exception {
        Deployment dep = createDeployment(DeploymentState.NEED_TO_DEPLOY);
        assertDoesNotThrow(() -> reconciler.reconcile(dep, context));
    }

    @Test
    void cleanup_deletesDeployment() {
        Deployment dep = createDeployment(DeploymentState.DEPLOYED);

        DeleteControl result = reconciler.cleanup(dep, context);

        verify(deploymentBus).deleteDeployment(dep.getSpec().getDeploymentSettings());
        assertNotNull(result);
    }

    @Test
    void cleanup_handlesException() {
        Deployment dep = createDeployment(DeploymentState.DEPLOYED);
        doThrow(new RuntimeException("test")).when(deploymentBus).deleteDeployment(any());

        DeleteControl result = reconciler.cleanup(dep, context);
        assertNotNull(result);
    }
}
