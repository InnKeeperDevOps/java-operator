package run.innkeeper.v1.deployment.crd;

import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;

public class DeploymentSpec {
    DeploymentSettings deploymentSettings;

    public DeploymentSettings getDeploymentSettings() {
        return deploymentSettings;
    }

    public void setDeploymentSettings(DeploymentSettings deploymentSettings) {
        this.deploymentSettings = deploymentSettings;
    }
}
