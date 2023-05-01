package run.innkeeper.events.actions.services;

import run.innkeeper.events.structure.ServiceEvent;
import run.innkeeper.v1.service.crd.Service;

public class UpdateService extends ServiceEvent {
    public UpdateService(Service service) {
        super(service);
    }
}
