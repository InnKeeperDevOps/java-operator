package run.innkeeper.api.dto.k8s;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.ServiceStatus;
import run.innkeeper.api.dto.ObjectMetaDTO;

public class K8sServiceDTO{
  @JsonProperty("spec")
  ServiceSpec spec;
  @JsonProperty("status")
  ServiceStatus status;

  ObjectMetaDTO meta;

  public K8sServiceDTO(Service service) {
    this.spec = service.getSpec();
    this.status = service.getStatus();
    this.meta = new ObjectMetaDTO(service.getMetadata());
  }

  public ServiceSpec getSpec() {
    return spec;
  }

  public void setSpec(ServiceSpec spec) {
    this.spec = spec;
  }

  public ServiceStatus getStatus() {
    return status;
  }

  public void setStatus(ServiceStatus status) {
    this.status = status;
  }

  public ObjectMetaDTO getMeta() {
    return meta;
  }

  public void setMeta(ObjectMetaDTO meta) {
    this.meta = meta;
  }
}
