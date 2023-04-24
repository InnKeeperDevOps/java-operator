package run.innkeeper.events.structure;

import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;

public class DeploymentEvent extends Event{
    Deployment deployment;
    public DeploymentEvent(Deployment deployment) {
        this.deployment = deployment;
    }
    public Deployment getDeployment() {
        return deployment;
    }
    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }
}
