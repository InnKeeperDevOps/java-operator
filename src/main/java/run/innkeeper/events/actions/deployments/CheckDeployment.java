package run.innkeeper.events.actions.deployments;

import run.innkeeper.events.structure.DeploymentEvent;
import run.innkeeper.v1.deployment.crd.Deployment;

public class CheckDeployment extends DeploymentEvent {


    public CheckDeployment(Deployment deployment) {
        super(deployment);
    }
}

