package run.innkeeper.events.actions.deployments;

import run.innkeeper.events.structure.DeploymentEvent;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.deployment.crd.Deployment;

public class UpdateDeployment extends DeploymentEvent {


    public UpdateDeployment(Deployment deployment) {
        super(deployment);
    }
}
