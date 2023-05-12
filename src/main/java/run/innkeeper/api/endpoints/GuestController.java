package run.innkeeper.api.endpoints;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import org.springframework.web.bind.annotation.*;
import run.innkeeper.api.dto.BuildDTO;
import run.innkeeper.api.dto.DeploymentDTO;
import run.innkeeper.api.dto.GuestDTO;
import run.innkeeper.api.dto.k8s.K8sDeploymentDTO;
import run.innkeeper.buses.DeploymentBus;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.guest.crd.GuestSpec;
import run.innkeeper.v1.guest.crd.GuestStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Guest controller.
 */
@RestController
@RequestMapping("/guest")
public class GuestController {
  /**
   * The Deployment bus.
   */
  DeploymentBus deploymentBus = DeploymentBus.get();
  /**
   * The K 8 s service.
   */
  K8sService k8sService = K8sService.get();

  /**
   * Gets guests.
   *
   * @return the guests
   */
  @GetMapping("/")
  public List<GuestDTO> getGuests() {
    return k8sService.getGuestClient().resources().map(guestResource -> new GuestDTO(guestResource.item())).collect(Collectors.toList());
  }

  /**
   * Gets guest.
   *
   * @param name      the name
   * @param namespace the namespace
   * @return the guest
   */
  @GetMapping("/{namespace}/{name}")
  public GuestDTO getGuest(
      @PathVariable("name") String name,
      @PathVariable("namespace") String namespace
  ) {
    Guest guest = k8sService.getGuestClient().resource(new Guest().setMetaData(namespace, name)).get();
    if (guest != null && guest.getSpec() != null) {
      return new GuestDTO(guest);
    }
    return null;
  }

  /**
   * Gets deployments.
   *
   * @param name      the name
   * @param namespace the namespace
   * @return the deployments
   */
  @GetMapping("/{namespace}/{name}/deployments")
  public List<DeploymentDTO> getDeployments(
      @PathVariable("name") String name,
      @PathVariable("namespace") String namespace
  ) {
    List<DeploymentDTO> returnList = new ArrayList<>();
    Guest guest = k8sService.getGuestClient().resource(new Guest().setMetaData(namespace, name)).get();
    if (guest != null && guest.getSpec() != null) {
      returnList.addAll(
          Arrays
              .stream(
                  guest
                      .getSpec()
                      .getDeploymentSettings()
              )
              .map(
                  deploymentSettings ->
                      new DeploymentDTO(k8sService
                          .getDeploymentClient()
                          .resource(new Deployment().setMetaData(guest.getMetadata().getNamespace(), deploymentSettings.getName()))
                          .get())
              )
              .collect(Collectors.toList()));
    }
    return returnList;
  }

  /**
   * Gets builds.
   *
   * @param name      the name
   * @param namespace the namespace
   * @return the builds
   */
  @GetMapping("/{namespace}/{name}/builds")
  public List<BuildDTO> getBuilds(
      @PathVariable("name") String name,
      @PathVariable("namespace") String namespace
  ) {
    List<BuildDTO> returnList = new ArrayList<>();
    Guest guest = k8sService.getGuestClient().resource(new Guest().setMetaData(namespace, name)).get();
    if (guest != null && guest.getSpec() != null) {
      returnList.addAll(
          Arrays
              .stream(
                  guest
                      .getSpec()
                      .getBuildSettings()
              )
              .map(
                  buildSettings ->
                      new BuildDTO(
                          k8sService
                              .getBuildClient()
                              .resource(
                                  new Build()
                                      .setMetaData(guest.getMetadata().getNamespace(), buildSettings.getName())
                              )
                              .get()
                      )

              )
              .collect(Collectors.toList()));
    }
    return returnList;
  }

  /**
   * Gets k 8 s deployments.
   *
   * @param name      the name
   * @param namespace the namespace
   * @return the k 8 s deployments
   */
  @GetMapping("/{namespace}/{name}/k8s/deployments")
  public List<K8sDeploymentDTO> getK8sDeployments(
      @PathVariable("name") String name,
      @PathVariable("namespace") String namespace
  ) {
    List<K8sDeploymentDTO> returnList = new ArrayList<>();
    Guest guest = k8sService.getGuestClient().resource(new Guest().setMetaData(namespace, name)).get();
    if (guest != null && guest.getSpec() != null) {
      returnList.addAll(
          Arrays
              .stream(
                  guest
                      .getSpec()
                      .getDeploymentSettings()
              )
              .map(
                  deploymentSettings -> {
                    Deployment deployment = k8sService
                        .getClient()
                        .resource(new Deployment().setMetaData(deploymentSettings.getNamespace(), deploymentSettings.getName()))
                        .get();
                    io.fabric8.kubernetes.api.model.apps.Deployment k8sDeployment = new DeploymentBuilder()
                        .withNewMetadata()
                        .withName(deployment.getSpec().getDeploymentSettings().getName())
                        .withNamespace(deployment.getSpec().getDeploymentSettings().getNamespace())
                        .endMetadata()
                        .build();
                    return new K8sDeploymentDTO(k8sService.getClient().resource(k8sDeployment).get());
                  }
              )
              .collect(Collectors.toList()));
    }
    return returnList;
  }
}
