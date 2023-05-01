package run.innkeeper.v1.service;

import io.javaoperatorsdk.operator.api.reconciler.*;
import run.innkeeper.buses.EventBus;
import run.innkeeper.buses.ServiceBus;
import run.innkeeper.events.actions.services.CreateService;
import run.innkeeper.events.actions.services.UpdateService;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.service.crd.Service;
import run.innkeeper.v1.service.crd.ServiceState;
import run.innkeeper.v1.service.crd.ServiceStatus;
import java.util.concurrent.TimeUnit;

@ControllerConfiguration()
public class ServiceReconciler implements Reconciler<Service>, Cleaner<Service> {

    ServiceBus serviceBus = ServiceBus.get();


    @Override
    public DeleteControl cleanup(Service service, Context<Service> context) {
        Logging.info("Deleting Service "+service.getSpec().getServiceSettings().getName());
        try {
            serviceBus.deleteService(service.getSpec().getServiceSettings());
        } catch (Exception e) { e.printStackTrace();}
        return DeleteControl.defaultDelete();
    }

    @Override
    public UpdateControl<Service> reconcile(Service service, Context<Service> context) throws Exception {
        Logging.debug("================== SERVICE RECONCILE =========================");
        EventBus eventBus = EventBus.get();
        if (service.getStatus() == null) {
            service.setStatus(new ServiceStatus());
            service.getStatus().setServiceState(ServiceState.NEED_TO_CREATE);
        } else {
            switch (service.getStatus().getServiceState()) {
                case RECREATE -> eventBus.get().fire(new UpdateService(service));
                case NEED_TO_CREATE -> eventBus.get().fire(new CreateService(service));
            }
        }
        return UpdateControl.patchStatus(service).rescheduleAfter(5, TimeUnit.SECONDS);
    }
}
