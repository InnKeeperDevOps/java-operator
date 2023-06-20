package run.innkeeper.extensions.gateway;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.*;
import run.innkeeper.extensions.Extension;
import run.innkeeper.extensions.ExtensionStructure;
import run.innkeeper.extensions.gateway.dto.HttpRouteDTO;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionState;

/**
 * The type Istio http route.
 */
@Extension("HTTP")
public class IstioHttpRoute implements ExtensionStructure{
  /**
   * The K 8 s service.
   */
  K8sService k8sService = K8sService.get();

  @Override
  public SimpleExtensionState create(SimpleExtension simpleExtension) {
    Logging.info("Simple extension creating....");
    HTTPRoute route = build(simpleExtension);
    if (route != null) {
      k8sService.getClient().resource(route).create();
    }
    return SimpleExtensionState.UP_TO_DATE;
  }

  @Override
  public SimpleExtensionState update(SimpleExtension simpleExtension) {
    Logging.info("Simple extension updating....");
    HTTPRoute route = build(simpleExtension);
    if (route != null) {
      k8sService.getClient().resource(route).delete();
      k8sService.getClient().resource(route).create();
    }
    return SimpleExtensionState.UP_TO_DATE;
  }

  @Override
  public SimpleExtensionState check(SimpleExtension simpleExtension) {
    Logging.info("Simple extension checking....");
    HTTPRoute route = build(simpleExtension);
    route = k8sService.getClient().resource(route).get();
    if (route == null) {
      return SimpleExtensionState.NEED_TO_CREATE;
    }
    return SimpleExtensionState.UP_TO_DATE;
  }

  @Override
  public HttpRouteDTO get(SimpleExtension simpleExtension) {
    String name = simpleExtension.getSpec().getName();
    String namespace = simpleExtension.getSpec().getData().get("namespace").getStrVal();
    HTTPRoute route = new HTTPRouteBuilder().withNewMetadata().withNamespace(namespace).withName(name).endMetadata().build();
    route = k8sService.getClient().resource(route).get();
    return new HttpRouteDTO(route);
  }

  @Override
  public void delete(SimpleExtension simpleExtension) {
    Logging.info("Simple extension deleting....");
    String name = simpleExtension.getSpec().getName();
    IntOrString namespace = simpleExtension.getSpec().getData().get("namespace");
    HTTPRoute httpRoute = new HTTPRouteBuilder()
        .withNewMetadata()
        .withName(name)
        .withNamespace(namespace.getStrVal())
        .endMetadata().build();
    k8sService.getClient().resource(httpRoute).delete();
  }

  /**
   * Build http route.
   *
   * @param simpleExtension the simple extension
   * @return the http route
   */
  HTTPRoute build(SimpleExtension simpleExtension) {
    String name = simpleExtension.getSpec().getName();
    IntOrString namespace = simpleExtension.getSpec().getData().get("namespace");
    IntOrString service = simpleExtension.getSpec().getData().get("service");
    IntOrString hostnames = simpleExtension.getSpec().getData().get("hostnames");
    IntOrString port = simpleExtension.getSpec().getData().get("port");
    IntOrString path = simpleExtension.getSpec().getData().get("path");
    IntOrString gateway = simpleExtension.getSpec().getData().get("gateway");
    if (name != null && namespace != null && service != null && hostnames != null && port != null && path != null && gateway != null) {
      return new HTTPRouteBuilder()
          .withNewMetadata()
          .withName(name)
          .withNamespace(namespace.getStrVal())
          .endMetadata()
          .withNewSpec()
          .addNewParentRef()
          .withName(gateway.getStrVal())
          .withNamespace(namespace.getStrVal())
          .endParentRef()
          .withHostnames(hostnames.getStrVal().split(","))
          .addNewRule()
          .addNewMatch()
          .withPath(
              new HTTPPathMatchBuilder()
                  .withValue(path.getStrVal())
                  .withType("PathPrefix")
                  .build()
          )
          .endMatch()
          .addToBackendRefs(
              new HTTPBackendRefBuilder()
                  .withKind(null)
                  .withGroup(null)
                  .withName(service.getStrVal())
                  .withNamespace(namespace.getStrVal())
                  .withPort(port.getIntVal())
                  .build()
          )
          .endRule()
          .endSpec()
          .build();
    }
    return null;
  }
}
