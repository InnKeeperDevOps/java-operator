package run.innkeeper.v1.guest.crd;

import com.fasterxml.jackson.annotation.JsonProperty;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;
import io.fabric8.generator.annotation.Required;
import run.innkeeper.v1.guest.crd.objects.TrafficSettings;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;

public class GuestSpec {
    @Required
    @JsonProperty("builds")
    BuildSettings[] buildSettings;
    @Required
    @JsonProperty("deployments")
    DeploymentSettings[] deploymentSettings;
    @JsonProperty("traffic")
    TrafficSettings[] trafficSettings;
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

    public ServiceSettings[] getServiceSettings() {
        return serviceSettings;
    }

    public void setServiceSettings(ServiceSettings[] serviceSettings) {
        this.serviceSettings = serviceSettings;
    }

    public TrafficSettings[] getTrafficSettings() {
        return trafficSettings;
    }

    public void setTrafficSettings(TrafficSettings[] trafficSettings) {
        this.trafficSettings = trafficSettings;
    }
}
