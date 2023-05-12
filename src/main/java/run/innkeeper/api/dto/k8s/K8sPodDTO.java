package run.innkeeper.api.dto.k8s;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import run.innkeeper.api.dto.ObjectMetaDTO;

public class K8sPodDTO {
    @JsonProperty("spec")
    PodSpec spec;
    @JsonProperty("status")
    PodStatus status;

    ObjectMetaDTO meta;

    public K8sPodDTO(Pod pod) {
        this.spec = pod.getSpec();
        this.meta = new ObjectMetaDTO(pod.getMetadata());
        this.status = pod.getStatus();
    }

    public PodSpec getSpec() {
        return spec;
    }

    public void setSpec(PodSpec spec) {
        this.spec = spec;
    }

    public PodStatus getStatus() {
        return status;
    }

    public void setStatus(PodStatus status) {
        this.status = status;
    }

    public ObjectMetaDTO getMeta() {
        return meta;
    }

    public void setMeta(ObjectMetaDTO meta) {
        this.meta = meta;
    }
}
