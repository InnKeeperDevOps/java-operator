package run.innkeeper.api.dto.k8s;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import run.innkeeper.api.dto.ObjectMetaDTO;

public class K8sDeploymentDTO {
    @JsonProperty("spec")
    DeploymentSpec spec;
    @JsonProperty("status")
    DeploymentStatus status;

    ObjectMetaDTO meta;

    public K8sDeploymentDTO(Deployment deployment) {
        this.spec = deployment.getSpec();
        this.meta = new ObjectMetaDTO(deployment.getMetadata());
        this.status = deployment.getStatus();
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

    public DeploymentSpec getSpec() {
        return spec;
    }

    public void setSpec(DeploymentSpec spec) {
        this.spec = spec;
    }
}
