package run.innkeeper.v1.guest.crd;

import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuestStatus {
    List<String> deploymentChangeHistory = new ArrayList<>();
    List<String> buildChangeHistory = new ArrayList<>();
    List<String> ingressChangeHistory = new ArrayList<>();

    List<String> servicesChangeHistory = new ArrayList<>();

    List<String> extensionsChangeHistory = new ArrayList<>();

    public List<String> getDeploymentChangeHistory() {
        return deploymentChangeHistory;
    }

    public void setDeploymentChangeHistory(List<String> deploymentChangeHistory) {
        this.deploymentChangeHistory = deploymentChangeHistory;
    }

    public List<String> getBuildChangeHistory() {
        return buildChangeHistory;
    }

    public void setBuildChangeHistory(List<String> buildChangeHistory) {
        this.buildChangeHistory = buildChangeHistory;
    }

    public List<String> getIngressChangeHistory() {
        return ingressChangeHistory;
    }

    public void setIngressChangeHistory(List<String> ingressChangeHistory) {
        this.ingressChangeHistory = ingressChangeHistory;
    }

    public List<String> getServicesChangeHistory() {
        return servicesChangeHistory;
    }

    public void setServicesChangeHistory(List<String> servicesChangeHistory) {
        this.servicesChangeHistory = servicesChangeHistory;
    }

    public List<String> getExtensionsChangeHistory() {
        return extensionsChangeHistory;
    }

    public void setExtensionsChangeHistory(List<String> extensionsChangeHistory) {
        this.extensionsChangeHistory = extensionsChangeHistory;
    }
}
