package run.innkeeper.v1.service.crd;

public class ServiceStatus {
    ServiceState serviceState;

    public ServiceState getServiceState() {
        return serviceState;
    }

    public void setServiceState(ServiceState serviceState) {
        this.serviceState = serviceState;
    }
}
