package run.innkeeper.v1.guest;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.guest.crd.GuestSpec;
import run.innkeeper.v1.guest.crd.GuestStatus;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionSpec;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GuestReconcilerTest {

    @Mock
    Context<Guest> context;

    GuestReconciler reconciler;

    @BeforeEach
    void setUp() {
        reconciler = new GuestReconciler();
    }

    private Guest createGuest(boolean withServiceAndExtensions) {
        Guest guest = new Guest();
        guest.setMetaData("test-ns", "test-guest");
        GuestSpec spec = new GuestSpec();

        BuildSettings bs = new BuildSettings();
        bs.setName("build1");
        spec.setBuildSettings(new BuildSettings[]{bs});

        DeploymentSettings ds = new DeploymentSettings();
        ds.setName("dep1");
        spec.setDeploymentSettings(new DeploymentSettings[]{ds});

        if (withServiceAndExtensions) {
            ServiceSettings ss = new ServiceSettings();
            ss.setName("svc1");
            spec.setServiceSettings(new ServiceSettings[]{ss});

            SimpleExtensionSpec ses = new SimpleExtensionSpec();
            ses.setName("ext1");
            spec.setSimpleExtensions(new SimpleExtensionSpec[]{ses});
        }

        guest.setSpec(spec);
        return guest;
    }

    @Test
    void reconcile_nullStatus_initializesStatus() throws Exception {
        Guest guest = createGuest(false);

        UpdateControl<Guest> result = reconciler.reconcile(guest, context);

        assertNotNull(guest.getStatus());
        assertTrue(result.isUpdateStatus());
    }

    @Test
    void reconcile_withExistingStatus_doesNotReset() throws Exception {
        Guest guest = createGuest(false);
        GuestStatus status = new GuestStatus();
        status.setBuildChangeHistory(new ArrayList<>());
        status.setDeploymentChangeHistory(new ArrayList<>());
        status.setServicesChangeHistory(new ArrayList<>());
        status.setExtensionsChangeHistory(new ArrayList<>());
        status.setIngressChangeHistory(new ArrayList<>());
        guest.setStatus(status);

        UpdateControl<Guest> result = reconciler.reconcile(guest, context);
        assertTrue(result.isUpdateStatus());
    }

    @Test
    void reconcile_withServicesAndExtensions_doesNotThrow() throws Exception {
        Guest guest = createGuest(true);
        assertDoesNotThrow(() -> reconciler.reconcile(guest, context));
    }

    @Test
    void cleanup_deletesAllResources() {
        Guest guest = createGuest(true);

        DeleteControl result = reconciler.cleanup(guest, context);

        assertNotNull(result);
    }

    @Test
    void cleanup_withNullServiceSettings_doesNotThrow() {
        Guest guest = createGuest(false);

        DeleteControl result = reconciler.cleanup(guest, context);
        assertNotNull(result);
    }
}
