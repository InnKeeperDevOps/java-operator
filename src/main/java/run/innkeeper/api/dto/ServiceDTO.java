package run.innkeeper.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.build.crd.BuildSpec;
import run.innkeeper.v1.build.crd.BuildStatus;
import run.innkeeper.v1.service.crd.Service;
import run.innkeeper.v1.service.crd.ServiceSpec;
import run.innkeeper.v1.service.crd.ServiceStatus;

/**
 * The type Service dto.
 */
public class ServiceDTO {
  /**
   * The Spec.
   */
  @JsonProperty("spec")
  ServiceSpec spec;
  /**
   * The Status.
   */
  @JsonProperty("status")
  ServiceStatus status;

  /**
   * The Meta.
   */
  ObjectMetaDTO meta;

  /**
   * Instantiates a new Service dto.
   *
   * @param service the service
   */
  public ServiceDTO(Service service) {
    this.spec = service.getSpec();
    this.status = service.getStatus();
    this.meta = new ObjectMetaDTO(service.getMetadata());
  }

  /**
   * Gets spec.
   *
   * @return the spec
   */
  public ServiceSpec getSpec() {
    return spec;
  }

  /**
   * Sets spec.
   *
   * @param spec the spec
   */
  public void setSpec(ServiceSpec spec) {
    this.spec = spec;
  }

  /**
   * Gets status.
   *
   * @return the status
   */
  public ServiceStatus getStatus() {
    return status;
  }

  /**
   * Sets status.
   *
   * @param status the status
   */
  public void setStatus(ServiceStatus status) {
    this.status = status;
  }

  /**
   * Gets meta.
   *
   * @return the meta
   */
  public ObjectMetaDTO getMeta() {
    return meta;
  }

  /**
   * Sets meta.
   *
   * @param meta the meta
   */
  public void setMeta(ObjectMetaDTO meta) {
    this.meta = meta;
  }
}
