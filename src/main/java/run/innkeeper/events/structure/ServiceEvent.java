package run.innkeeper.events.structure;

import run.innkeeper.v1.service.crd.Service;

public class ServiceEvent extends Event {
    Service service;

    public ServiceEvent(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
