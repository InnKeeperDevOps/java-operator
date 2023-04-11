package run.innkeeper.v1.guest;

import io.javaoperatorsdk.operator.api.reconciler.*;
import run.innkeeper.buses.EventBus;
import run.innkeeper.events.actions.guests.CheckGuestBuildChanges;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;

import java.util.concurrent.TimeUnit;

@ControllerConfiguration(maxReconciliationInterval = @MaxReconciliationInterval(
    interval = 15,
    timeUnit = TimeUnit.SECONDS))
public class GuestReconciler implements Reconciler<Guest> {

    K8sService k8sService = K8sService.get();

    @Override
    public UpdateControl<Guest> reconcile(Guest guest, Context<Guest> context) throws Exception {
        Logging.debug("================== GUEST RECONCILE =========================");
        BuildSettings[] buildSettingsDefinitions = guest.getSpec().getBuildSettings();
        for (int i = 0; i < buildSettingsDefinitions.length; i++) {
            EventBus.get().fire(new CheckGuestBuildChanges(guest, buildSettingsDefinitions[i]));
        }
        return UpdateControl.patchStatus(guest);
    }
    // Return the changed resource, so it gets updated. See javadoc for details.

}