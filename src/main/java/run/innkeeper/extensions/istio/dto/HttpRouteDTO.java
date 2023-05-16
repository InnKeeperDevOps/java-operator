package run.innkeeper.extensions.istio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRoute;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRouteSpec;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRouteStatus;
import run.innkeeper.api.dto.ObjectMetaDTO;

public class HttpRouteDTO{
  @JsonProperty("spec")
  HTTPRouteSpec spec;
  @JsonProperty("status")
  HTTPRouteStatus status;

  ObjectMetaDTO meta;

  public HttpRouteDTO(HTTPRoute route) {
    this.spec = route.getSpec();
    this.status = route.getStatus();
    this.meta = new ObjectMetaDTO(route.getMetadata());
  }

  public HTTPRouteSpec getSpec() {
    return spec;
  }

  public void setSpec(HTTPRouteSpec spec) {
    this.spec = spec;
  }

  public HTTPRouteStatus getStatus() {
    return status;
  }

  public void setStatus(HTTPRouteStatus status) {
    this.status = status;
  }

  public ObjectMetaDTO getMeta() {
    return meta;
  }

  public void setMeta(ObjectMetaDTO meta) {
    this.meta = meta;
  }
}
