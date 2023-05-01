package run.innkeeper.events.actions.services;

import run.innkeeper.events.structure.ServiceEvent;
import run.innkeeper.v1.service.crd.Service;

public class CreateService extends ServiceEvent {
    public CreateService(Service service) {
        super(service);
    }
}
