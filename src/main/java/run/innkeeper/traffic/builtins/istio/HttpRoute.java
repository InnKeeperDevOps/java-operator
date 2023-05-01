package run.innkeeper.traffic.builtins.istio;

import run.innkeeper.services.K8sService;
import run.innkeeper.traffic.TrafficStructure;
import run.innkeeper.v1.guest.crd.objects.TrafficSettings;

public class HttpRoute implements TrafficStructure {
    K8sService k8sService = new K8sService();

    @Override
    public boolean create(TrafficSettings trafficSettings) {
        return false;
    }

    @Override
    public boolean update(TrafficSettings trafficSettings) {
        return false;
    }

    @Override
    public boolean delete(TrafficSettings trafficSettings) {
        return false;
    }
}
