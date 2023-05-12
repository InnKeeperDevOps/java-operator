package run.innkeeper.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.deployment.crd.DeploymentSpec;
import run.innkeeper.v1.deployment.crd.DeploymentState;
import run.innkeeper.v1.deployment.crd.DeploymentStatus;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.guest.crd.GuestSpec;
import run.innkeeper.v1.guest.crd.GuestStatus;

public class DeploymentDTO {
    @JsonProperty("spec")
    DeploymentSpec spec;
    @JsonProperty("status")
    DeploymentStatus status;

    ObjectMetaDTO meta;

    public DeploymentDTO(Deployment deployment) {
        this.spec = deployment.getSpec();
        this.status = deployment.getStatus();
        this.meta = new ObjectMetaDTO(deployment.getMetadata());
    }

    public DeploymentSpec getSpec() {
        return spec;
    }

    public void setSpec(DeploymentSpec spec) {
        this.spec = spec;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    public ObjectMetaDTO getMeta() {
        return meta;
    }

    public void setMeta(ObjectMetaDTO meta) {
        this.meta = meta;
    }
}
