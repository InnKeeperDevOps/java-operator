package run.innkeeper.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import run.innkeeper.v1.service.crd.Service;
import run.innkeeper.v1.service.crd.ServiceSpec;
import run.innkeeper.v1.service.crd.ServiceStatus;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionSpec;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionStatus;

/**
 * The type Simple extension dto.
 */
public class SimpleExtensionDTO{
  /**
   * The Spec.
   */
  @JsonProperty("spec")
  SimpleExtensionSpec spec;
  /**
   * The Status.
   */
  @JsonProperty("status")
  SimpleExtensionStatus status;

  /**
   * The Meta.
   */
  ObjectMetaDTO meta;

  /**
   * Instantiates a new Simple extension dto.
   *
   * @param simpleExtension the simple extension
   */
  public SimpleExtensionDTO(SimpleExtension simpleExtension) {
    this.spec = simpleExtension.getSpec();
    this.status = simpleExtension.getStatus();
    this.meta = new ObjectMetaDTO(simpleExtension.getMetadata());
  }

  /**
   * Gets spec.
   *
   * @return the spec
   */
  public SimpleExtensionSpec getSpec() {
    return spec;
  }

  /**
   * Sets spec.
   *
   * @param spec the spec
   */
  public void setSpec(SimpleExtensionSpec spec) {
    this.spec = spec;
  }

  /**
   * Gets status.
   *
   * @return the status
   */
  public SimpleExtensionStatus getStatus() {
    return status;
  }

  /**
   * Sets status.
   *
   * @param status the status
   */
  public void setStatus(SimpleExtensionStatus status) {
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
