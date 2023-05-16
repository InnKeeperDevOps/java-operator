package run.innkeeper.api.endpoints;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import org.springframework.web.bind.annotation.*;
import run.innkeeper.api.dto.BuildDTO;
import run.innkeeper.api.dto.DeploymentDTO;
import run.innkeeper.api.dto.GuestDTO;
import run.innkeeper.api.dto.ServiceDTO;
import run.innkeeper.api.dto.SimpleExtensionDTO;
import run.innkeeper.api.dto.k8s.K8sDeploymentDTO;
import run.innkeeper.buses.DeploymentBus;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.guest.crd.GuestSpec;
import run.innkeeper.v1.guest.crd.GuestStatus;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.service.crd.Service;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionSpec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Guest controller.
 */
@RestController
@RequestMapping("/guest")
public class GuestController{
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
  @GetMapping("/{namespace}/{name}/")
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
              .toList());
    }
    return returnList;
  }

  /**
   * Gets deployments.
   *
   * @param name               the name
   * @param namespace          the namespace
   * @param deploymentSettings the deployment settings
   * @return the deployments
   */
  @PutMapping("/{namespace}/{name}/deployments")
  public List<DeploymentDTO> saveDeployments(
      @PathVariable("name") String name,
      @PathVariable("namespace") String namespace,
      @RequestBody List<DeploymentSettings> deploymentSettings
  ) {
    List<DeploymentDTO> returnList = new ArrayList<>();
    Guest guest = k8sService.getGuestClient().resource(new Guest().setMetaData(namespace, name)).get();
    if (guest != null && guest.getSpec() != null) {
      guest
          .getSpec()
          .setDeploymentSettings(
              Arrays.stream(guest.getSpec().getDeploymentSettings())
                  .map(oldDeploymentSetting -> {
                    Optional<DeploymentSettings> newDeploymentSetting = deploymentSettings.stream().filter(d -> d.getName().equals(oldDeploymentSetting.getName())).findFirst();
                    if (newDeploymentSetting.isPresent()) {
                      return newDeploymentSetting.get();
                    }
                    return oldDeploymentSetting;
                  })
                  .collect(Collectors.toList())
                  .toArray(new DeploymentSettings[0])
          );
      k8sService.getGuestClient().resource(guest).update();
      returnList.addAll(
          Arrays
              .stream(
                  guest
                      .getSpec()
                      .getDeploymentSettings()
              )
              .map(
                  deploymentSetting ->
                      new DeploymentDTO(k8sService
                                            .getDeploymentClient()
                                            .resource(new Deployment().setMetaData(guest.getMetadata().getNamespace(), deploymentSetting.getName()))
                                            .get())
              )
              .toList());
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
              .toList());
    }
    return returnList;
  }

  /**
   * Gets services.
   *
   * @param name      the name
   * @param namespace the namespace
   * @return the services
   */
  @GetMapping("/{namespace}/{name}/services")
  public List<ServiceDTO> getServices(
      @PathVariable("name") String name,
      @PathVariable("namespace") String namespace
  ) {
    List<ServiceDTO> returnList = new ArrayList<>();
    Guest guest = k8sService.getGuestClient().resource(new Guest().setMetaData(namespace, name)).get();
    if (guest != null && guest.getSpec() != null) {
      returnList.addAll(
          Arrays
              .stream(
                  guest
                      .getSpec()
                      .getServiceSettings()
              )
              .map(
                  serviceSettings ->
                      new ServiceDTO(
                          k8sService
                              .getServiceClient()
                              .resource(
                                  new Service()
                                      .setMetaData(guest.getMetadata().getNamespace(), serviceSettings.getName())
                              )
                              .get()
                      )

              )
              .toList());
    }
    return returnList;
  }

  /**
   * Gets simple extensions.
   *
   * @param name      the name
   * @param namespace the namespace
   * @return the simple extensions
   */
  @GetMapping("/{namespace}/{name}/extensions")
  public List<SimpleExtensionDTO> getSimpleExtensions(
      @PathVariable("name") String name,
      @PathVariable("namespace") String namespace
  ) {
    List<SimpleExtensionDTO> returnList = new ArrayList<>();
    Guest guest = k8sService.getGuestClient().resource(new Guest().setMetaData(namespace, name)).get();
    if (guest != null && guest.getSpec() != null) {
      returnList.addAll(
          Arrays
              .stream(
                  guest
                      .getSpec()
                      .getSimpleExtensions()
              )
              .map(
                  simpleExtension ->
                      new SimpleExtensionDTO(
                          k8sService
                              .getSimpleExtensionClient()
                              .resource(
                                  new SimpleExtension()
                                      .setMetaData(guest.getMetadata().getNamespace(), simpleExtension.getName())
                              )
                              .get()
                      )

              )
              .toList());
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
              .toList());
    }
    return returnList;
  }

  /**
   * Save guest guest dto.
   *
   * @param name      the name
   * @param namespace the namespace
   * @param guestSpec the guest spec
   * @return the guest dto
   */
  @PutMapping("/{namespace}/{name}/")
  public GuestDTO saveGuest(@PathVariable String name, @PathVariable String namespace, @RequestBody GuestSpec
      guestSpec) {
    Guest guest = k8sService.getGuestClient().resource(new Guest().setMetaData(namespace, name)).get();
    guest.setSpec(guestSpec);
    return new GuestDTO(k8sService.getGuestClient().resource(guest).update());
  }

  /**
   * Save services guest dto.
   *
   * @param name            the name
   * @param namespace       the namespace
   * @param serviceSettings the service settings
   * @return the guest dto
   */
  @PutMapping("/{namespace}/{name}/save/services")
  public GuestDTO saveServices(@PathVariable String name, @PathVariable String
      namespace, @RequestBody ServiceSettings[] serviceSettings) {
    Guest guest = k8sService.getGuestClient().resource(new Guest().setMetaData(namespace, name)).get();
    guest.getSpec().setServiceSettings(serviceSettings);
    return new GuestDTO(k8sService.getGuestClient().resource(guest).update());
  }

  /**
   * Save builds guest dto.
   *
   * @param name          the name
   * @param namespace     the namespace
   * @param buildSettings the build settings
   * @return the guest dto
   */
  @PutMapping("/{namespace}/{name}/save/builds")
  public GuestDTO saveBuilds(@PathVariable String name, @PathVariable String namespace, @RequestBody BuildSettings[]
      buildSettings) {
    Guest guest = k8sService.getGuestClient().resource(new Guest().setMetaData(namespace, name)).get();
    guest.getSpec().setBuildSettings(buildSettings);
    return new GuestDTO(k8sService.getGuestClient().resource(guest).update());
  }

  /**
   * Save deployments guest dto.
   *
   * @param name               the name
   * @param namespace          the namespace
   * @param deploymentSettings the deployment settings
   * @return the guest dto
   */
  @PutMapping("/{namespace}/{name}/save/deployments")
  public GuestDTO saveDeployments(@PathVariable String name, @PathVariable String
      namespace, @RequestBody DeploymentSettings[] deploymentSettings) {
    Guest guest = k8sService.getGuestClient().resource(new Guest().setMetaData(namespace, name)).get();
    guest.getSpec().setDeploymentSettings(deploymentSettings);
    return new GuestDTO(k8sService.getGuestClient().resource(guest).update());
  }

  /**
   * Save extensions guest dto.
   *
   * @param name                 the name
   * @param namespace            the namespace
   * @param simpleExtensionSpecs the simple extension specs
   * @return the guest dto
   */
  @PutMapping("/{namespace}/{name}/save/extensions")
  public GuestDTO saveExtensions(@PathVariable String name, @PathVariable String
      namespace, @RequestBody SimpleExtensionSpec[] simpleExtensionSpecs) {
    Guest guest = k8sService.getGuestClient().resource(new Guest().setMetaData(namespace, name)).get();
    guest.getSpec().setSimpleExtensions(simpleExtensionSpecs);
    return new GuestDTO(k8sService.getGuestClient().resource(guest).update());
  }
}
