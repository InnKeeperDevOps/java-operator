package run.innkeeper.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.build.crd.BuildSpec;
import run.innkeeper.v1.build.crd.BuildStatus;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.guest.crd.GuestSpec;
import run.innkeeper.v1.guest.crd.GuestStatus;

public class BuildDTO {
  @JsonProperty("spec")
  BuildSpec spec;
  @JsonProperty("status")
  BuildStatus status;

  ObjectMetaDTO meta;

  public BuildDTO(Build build) {
    this.spec = build.getSpec();
    this.status = build.getStatus();
    this.meta = new ObjectMetaDTO(build.getMetadata());
  }

  public BuildSpec getSpec() {
    return spec;
  }

  public void setSpec(BuildSpec spec) {
    this.spec = spec;
  }

  public BuildStatus getStatus() {
    return status;
  }

  public void setStatus(BuildStatus status) {
    this.status = status;
  }

  public ObjectMetaDTO getMeta() {
    return meta;
  }

  public void setMeta(ObjectMetaDTO meta) {
    this.meta = meta;
  }
}
