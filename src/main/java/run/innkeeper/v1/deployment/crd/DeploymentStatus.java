package run.innkeeper.v1.deployment.crd;

import run.innkeeper.v1.build.crd.BuildState;

public class DeploymentStatus {

    DeploymentState state;

    public DeploymentState getState() {
        return state;
    }

    public void setState(DeploymentState state) {
        this.state = state;
    }
}
