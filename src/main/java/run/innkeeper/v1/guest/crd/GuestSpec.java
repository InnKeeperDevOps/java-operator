package run.innkeeper.v1.guest.crd;

import com.fasterxml.jackson.annotation.JsonProperty;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;
import io.fabric8.generator.annotation.Required;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionSpec;

public class GuestSpec {
    @Required
    @JsonProperty("builds")
    BuildSettings[] buildSettings;
    @Required
    @JsonProperty("deployments")
    DeploymentSettings[] deploymentSettings;
    @JsonProperty("services")
    ServiceSettings[] serviceSettings;

    @JsonProperty("ext")
    SimpleExtensionSpec[] simpleExtensions;

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

    public SimpleExtensionSpec[] getSimpleExtensions() {
        return simpleExtensions;
    }

    public void setSimpleExtensions(SimpleExtensionSpec[] simpleExtensions) {
        this.simpleExtensions = simpleExtensions;
    }
}
