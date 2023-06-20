package run.innkeeper.events.actions.deployments;

import run.innkeeper.events.structure.DeploymentEvent;
import run.innkeeper.v1.deployment.crd.Deployment;

public class CreateDeployment extends DeploymentEvent {
    public CreateDeployment(Deployment deployment) {
        super(deployment);
    }
}
