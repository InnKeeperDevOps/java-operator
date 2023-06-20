package run.innkeeper.v1.simpleExtensions;

import io.javaoperatorsdk.operator.api.reconciler.*;
import run.innkeeper.buses.EventBus;
import run.innkeeper.events.extension.ExtensionCheck;
import run.innkeeper.events.extension.ExtensionCreate;
import run.innkeeper.events.extension.ExtensionDelete;
import run.innkeeper.events.extension.ExtensionUpdate;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionState;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionStatus;

import java.util.concurrent.TimeUnit;

@ControllerConfiguration()
public class SimpleExtensionReconciler implements Reconciler<SimpleExtension>, Cleaner<SimpleExtension>{

  EventBus eventBus = EventBus.get();
  K8sService k8sService = K8sService.get();

  @Override
  public DeleteControl cleanup(SimpleExtension resource, Context<SimpleExtension> context) {
    eventBus.fire(new ExtensionDelete(resource));
    return DeleteControl.defaultDelete();
  }

  @Override
  public UpdateControl<SimpleExtension> reconcile(SimpleExtension resource, Context<SimpleExtension> context) throws Exception {
    Logging.debug("================== SIMPLE EXTENSION RECONCILE =========================");

    if (resource.getStatus() == null) {
      resource.setStatus(new SimpleExtensionStatus());
      resource.getStatus().setCurrentState(SimpleExtensionState.NEED_TO_CREATE);
    } else {
      switch (resource.getStatus().getCurrentState()) {
        case NEED_TO_UPDATE -> eventBus.get().fire(new ExtensionUpdate(resource));
        case NEED_TO_CREATE -> eventBus.get().fire(new ExtensionCreate(resource));
        case UP_TO_DATE -> eventBus.get().fire(new ExtensionCheck(resource));
      }
    }
    return UpdateControl.updateStatus(resource).rescheduleAfter(3, TimeUnit.SECONDS);
  }
}
