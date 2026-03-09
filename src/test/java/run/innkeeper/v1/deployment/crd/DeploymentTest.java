package run.innkeeper.v1.deployment.crd;

import org.junit.jupiter.api.Test;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentTest {

    @Test
    void setMetaData_setsNameAndNamespace() {
        Deployment dep = new Deployment();
        dep.setMetaData("test-ns", "test-name");
        assertEquals("test-ns", dep.getMetadata().getNamespace());
        assertEquals("test-name", dep.getMetadata().getName());
    }

    @Test
    void deploymentSpec_getSetDeploymentSettings() {
        DeploymentSpec spec = new DeploymentSpec();
        DeploymentSettings ds = new DeploymentSettings();
        ds.setName("dep1");
        ds.setNamespace("ns1");
        ds.setReplicas(3);
        spec.setDeploymentSettings(ds);
        assertEquals("dep1", spec.getDeploymentSettings().getName());
        assertEquals("ns1", spec.getDeploymentSettings().getNamespace());
        assertEquals(3, spec.getDeploymentSettings().getReplicas());
    }

    @Test
    void deploymentStatus_getSetState() {
        DeploymentStatus status = new DeploymentStatus();
        status.setState(DeploymentState.DEPLOYED);
        assertEquals(DeploymentState.DEPLOYED, status.getState());
    }

    @Test
    void deploymentState_allValues() {
        assertEquals(5, DeploymentState.values().length);
        assertNotNull(DeploymentState.valueOf("DEPLOYING"));
        assertNotNull(DeploymentState.valueOf("DEPLOYED"));
        assertNotNull(DeploymentState.valueOf("NEED_TO_DEPLOY"));
        assertNotNull(DeploymentState.valueOf("REDEPLOY"));
        assertNotNull(DeploymentState.valueOf("FAILED"));
    }

    @Test
    void deploymentList_instantiation() {
        DeploymentList list = new DeploymentList();
        assertNotNull(list);
    }

    @Test
    void deploymentSettings_containers() {
        DeploymentSettings ds = new DeploymentSettings();
        ds.setContainers(new java.util.ArrayList<>());
        assertNotNull(ds.getContainers());
        assertTrue(ds.getContainers().isEmpty());
    }

    @Test
    void deploymentSettings_volumes() {
        DeploymentSettings ds = new DeploymentSettings();
        ds.setVolumes(new java.util.ArrayList<>());
        assertNotNull(ds.getVolumes());
        assertTrue(ds.getVolumes().isEmpty());
    }
}
