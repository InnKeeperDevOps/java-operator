package run.innkeeper.api.endpoints;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.annotations.UserAuthorized;
import run.innkeeper.api.dto.DeploymentDTO;
import run.innkeeper.api.dto.k8s.K8sPodDTO;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.deployment.crd.Deployment;

/**
 * The type Deployment controller.
 */
@RestController
@RequestMapping("/deployment")
public class DeploymentController {
  /**
   * The K 8 s service.
   */
  K8sService k8sService = K8sService.get();

  /**
   * Gets deployments.
   *
   * @return the deployments
   */
  @GetMapping("/")
  @UserAuthorized("deployments.list")
  public List<DeploymentDTO> getDeployments() {
    return k8sService
        .getDeploymentClient()
        .list()
        .getItems()
        .stream()
        .map(d -> new DeploymentDTO(d))
        .collect(Collectors.toList());
  }

  /**
   * Gets deployment.
   *
   * @param name      the name
   * @param namespace the namespace
   * @return the deployment
   */
  @GetMapping("/{namespace}/{name}/pods")
  @UserAuthorized("deployment.pods")
  public List<K8sPodDTO> getDeployment(
      @PathVariable("name") String name,
      @PathVariable("namespace") String namespace
  ) {
    List<K8sPodDTO> returnList = new ArrayList<>();
    Deployment deployment = k8sService
        .getClient()
        .resource(new Deployment().setMetaData(namespace, name))
        .get();
    if (deployment != null && deployment.getSpec() != null) {
      Map<String, String> labels = new HashMap<>();
      labels.put("app-selector", deployment.getSpec().getDeploymentSettings().getName());
      returnList.addAll(
          k8sService
              .getClient()
              .pods()
              .withLabels(labels)
              .list()
              .getItems()
              .stream()
              .map(pod -> new K8sPodDTO(pod)).collect(Collectors.toList()));
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
  @GetMapping("/{namespace}/{name}/")
  @UserAuthorized("deployment.get")
  public DeploymentDTO getDeployments(
      @PathVariable("name") String name,
      @PathVariable("namespace") String namespace
  ) {
    Deployment deployment = k8sService
        .getClient()
        .resource(
            new Deployment()
                .setMetaData(namespace, name)
        )
        .get();
    if (deployment != null && deployment.getSpec() != null) {
      return new DeploymentDTO(deployment);
    }
    return null;
  }

}
