package run.innkeeper.controllers;

import io.fabric8.kubernetes.api.model.Service;
import run.innkeeper.buses.ServiceBus;
import run.innkeeper.events.actions.services.CreateService;
import run.innkeeper.events.actions.services.UpdateService;
import run.innkeeper.events.structure.Trigger;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.service.crd.ServiceState;

public class ServiceController {
    ServiceBus serviceBus = ServiceBus.get();
    @Trigger(CreateService.class)
    public void createService(CreateService event){
        ServiceSettings serviceSettings = event.getService().getSpec().getServiceSettings();
        Service service = serviceBus.getService(serviceSettings);
        if(service==null){
            serviceBus.createService(serviceSettings);
            event.getService().getStatus().setServiceState(ServiceState.CREATED);
        }
    }

    @Trigger(UpdateService.class)
    public void updateService(UpdateService event){
        ServiceSettings serviceSettings = event.getService().getSpec().getServiceSettings();
        Service service = serviceBus.getService(serviceSettings);
        if(service!=null){
            serviceBus.updateService(serviceSettings);
            event.getService().getStatus().setServiceState(ServiceState.CREATED);
        }
    }
}
