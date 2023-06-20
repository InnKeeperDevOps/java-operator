package run.innkeeper.v1.guest;


import io.javaoperatorsdk.operator.api.reconciler.Cleaner;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import run.innkeeper.buses.EventBus;
import run.innkeeper.events.actions.guests.*;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.guest.crd.GuestStatus;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionSpec;

import java.util.concurrent.TimeUnit;

@ControllerConfiguration()
public class GuestReconciler implements Reconciler<Guest>, Cleaner<Guest>{

  K8sService k8sService = K8sService.get();

  @Override
  public UpdateControl<Guest> reconcile(Guest guest, Context<Guest> context) throws Exception {
    Logging.debug("================== GUEST RECONCILE =========================");
    if (guest.getStatus() == null) {
      guest.setStatus(new GuestStatus());
    }
    BuildSettings[] buildSettingsDefinitions = guest.getSpec().getBuildSettings();
    for (int i = 0; i < buildSettingsDefinitions.length; i++) {
      EventBus.get().fire(new CheckGuestBuildChanges(guest, buildSettingsDefinitions[i]));
    }
    DeploymentSettings[] deploymentSettings = guest.getSpec().getDeploymentSettings();
    for (int i = 0; i < deploymentSettings.length; i++) {
      EventBus.get().fire(new CheckGuestDeploymentChanges(guest, deploymentSettings[i]));
    }
    ServiceSettings[] serviceSettings = guest.getSpec().getServiceSettings();
    if (serviceSettings != null) {
      for (int i = 0; i < serviceSettings.length; i++) {
        EventBus.get().fire(new CheckGuestServiceChanges(guest, serviceSettings[i]));
      }
    }
    SimpleExtensionSpec[] simpleExtensions = guest.getSpec().getSimpleExtensions();
    if (simpleExtensions != null) {
      for (int i = 0; i < simpleExtensions.length; i++) {
        EventBus.get().fire(new CheckGuestExtensionChanges(guest, simpleExtensions[i]));
      }
    }
    return UpdateControl.updateStatus(guest).rescheduleAfter(3, TimeUnit.SECONDS);
  }

  // Return the changed resource, so it gets updated. See javadoc for details.
  public DeleteControl cleanup(Guest guest, Context<Guest> context) {
    Logging.info("DELETING GUEST");
    BuildSettings[] buildSettingsDefinitions = guest.getSpec().getBuildSettings();
    for (int i = 0; i < buildSettingsDefinitions.length; i++) {
      EventBus.get().fire(new DeleteGuestBuild(guest, buildSettingsDefinitions[i]));
    }
    DeploymentSettings[] deploymentSettings = guest.getSpec().getDeploymentSettings();
    for (int i = 0; i < deploymentSettings.length; i++) {
      EventBus.get().fire(new DeleteGuestDeployment(guest, deploymentSettings[i]));
    }
    ServiceSettings[] serviceSettings = guest.getSpec().getServiceSettings();
    if (serviceSettings != null) {
      for (int i = 0; i < serviceSettings.length; i++) {
        EventBus.get().fire(new DeleteGuestService(guest, serviceSettings[i]));
      }
    }
    SimpleExtensionSpec[] simpleExtensions = guest.getSpec().getSimpleExtensions();
    if (simpleExtensions != null) {
      for (int i = 0; i < simpleExtensions.length; i++) {
        EventBus.get().fire(new DeleteGuestExtension(guest, simpleExtensions[i]));
      }
    }
    return DeleteControl.defaultDelete();
  }
}