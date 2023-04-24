package run.innkeeper.v1.deployment.crd;

public enum DeploymentState {
    DEPLOYING("DG"),
    DEPLOYED("DD"),
    NEED_TO_DEPLOY("NTD"),
    REDEPLOY("RD"),
    FAILED("F");

    String value;

    DeploymentState(String value) {
        this.value = value;
    }
}
