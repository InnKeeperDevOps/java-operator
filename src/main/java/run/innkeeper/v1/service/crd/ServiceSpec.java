package run.innkeeper.v1.service.crd;

import run.innkeeper.v1.guest.crd.objects.ServiceSettings;

public class ServiceSpec {
    ServiceSettings serviceSettings;

    public ServiceSettings getServiceSettings() {
        return serviceSettings;
    }

    public void setServiceSettings(ServiceSettings serviceSettings) {
        this.serviceSettings = serviceSettings;
    }
}
