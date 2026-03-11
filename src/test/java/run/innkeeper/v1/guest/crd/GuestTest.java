package run.innkeeper.v1.guest.crd;

import org.junit.jupiter.api.Test;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionSpec;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class GuestTest {

    @Test
    void setMetaData_setsNameAndNamespace() {
        Guest guest = new Guest();
        guest.setMetaData("test-ns", "test-name");
        assertEquals("test-ns", guest.getMetadata().getNamespace());
        assertEquals("test-name", guest.getMetadata().getName());
    }

    @Test
    void guestSpec_buildSettings() {
        GuestSpec spec = new GuestSpec();
        BuildSettings bs = new BuildSettings();
        bs.setName("build1");
        spec.setBuildSettings(new BuildSettings[]{bs});
        assertEquals(1, spec.getBuildSettings().length);
        assertEquals("build1", spec.getBuildSettings()[0].getName());
    }

    @Test
    void guestSpec_deploymentSettings() {
        GuestSpec spec = new GuestSpec();
        DeploymentSettings ds = new DeploymentSettings();
        ds.setName("deploy1");
        spec.setDeploymentSettings(new DeploymentSettings[]{ds});
        assertEquals(1, spec.getDeploymentSettings().length);
    }

    @Test
    void guestSpec_serviceSettings() {
        GuestSpec spec = new GuestSpec();
        ServiceSettings ss = new ServiceSettings();
        ss.setName("svc1");
        spec.setServiceSettings(new ServiceSettings[]{ss});
        assertEquals(1, spec.getServiceSettings().length);
    }

    @Test
    void guestSpec_simpleExtensions() {
        GuestSpec spec = new GuestSpec();
        SimpleExtensionSpec ses = new SimpleExtensionSpec();
        ses.setName("ext1");
        spec.setSimpleExtensions(new SimpleExtensionSpec[]{ses});
        assertEquals(1, spec.getSimpleExtensions().length);
    }

    @Test
    void guestStatus_changeHistory() {
        GuestStatus status = new GuestStatus();
        status.setBuildChangeHistory(new ArrayList<>(Arrays.asList("change1")));
        status.setDeploymentChangeHistory(new ArrayList<>(Arrays.asList("change2")));
        status.setServicesChangeHistory(new ArrayList<>(Arrays.asList("change3")));
        status.setExtensionsChangeHistory(new ArrayList<>(Arrays.asList("change4")));
        status.setIngressChangeHistory(new ArrayList<>(Arrays.asList("change5")));

        assertEquals(1, status.getBuildChangeHistory().size());
        assertEquals(1, status.getDeploymentChangeHistory().size());
        assertEquals(1, status.getServicesChangeHistory().size());
        assertEquals(1, status.getExtensionsChangeHistory().size());
        assertEquals(1, status.getIngressChangeHistory().size());
    }

    @Test
    void guestList_instantiation() {
        GuestList list = new GuestList();
        assertNotNull(list);
    }
}
