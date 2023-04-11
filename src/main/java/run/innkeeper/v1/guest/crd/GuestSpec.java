package run.innkeeper.v1.guest.crd;

import com.fasterxml.jackson.annotation.JsonProperty;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;
import run.innkeeper.v1.guest.crd.objects.IngressSettings;
import io.fabric8.generator.annotation.Required;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;

public class GuestSpec {
    @Required
    @JsonProperty("builds")
    BuildSettings[] buildSettings;
    @Required
    @JsonProperty("deployments")
    DeploymentSettings[] deploymentSettings;
    @JsonProperty("ingress")
    IngressSettings[] ingressSettings;
    @JsonProperty("services")
    ServiceSettings[] serviceSettings;

    public BuildSettings[] getBuildSettings() {
        return buildSettings;
    }

    public void setBuildSettings(BuildSettings[] buildSettings) {
        this.buildSettings = buildSettings;
    }

    public DeploymentSettings[] getDeploymentSettings() {
        return deploymentSettings;
    }

    public void setDeploymentSettings(DeploymentSettings[] deploymentSettings) {
        this.deploymentSettings = deploymentSettings;
    }

    public IngressSettings[] getIngressSettings() {
        return ingressSettings;
    }

    public void setIngressSettings(IngressSettings[] ingressSettings) {
        this.ingressSettings = ingressSettings;
    }

    public ServiceSettings[] getServiceSettings() {
        return serviceSettings;
    }

    public void setServiceSettings(ServiceSettings[] serviceSettings) {
        this.serviceSettings = serviceSettings;
    }
}
