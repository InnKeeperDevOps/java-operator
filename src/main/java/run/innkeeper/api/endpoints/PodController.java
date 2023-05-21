package run.innkeeper.api.endpoints;

import ch.qos.logback.classic.Logger;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.auth.UserAuthorized;
import run.innkeeper.api.dto.k8s.K8sPodDTO;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.deployment.crd.Deployment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Pod controller.
 */
@RestController
@RequestMapping("/pod")
public class PodController {
  /**
   * The K 8 s service.
   */
  K8sService k8sService = K8sService.get();

  /**
   * Pod list.
   *
   * @return the list
   */
  @GetMapping("/")
  @UserAuthorized("pod.list")
  public List<K8sPodDTO> podList() {
    List<Pod> pods = k8sService.getClient().pods().inAnyNamespace().list().getItems();
    if (pods != null) {
      List<Deployment> deployments = k8sService.getDeploymentClient().list().getItems();
      List<String> validPodNames = deployments
          .stream()
          .map(deployment -> Arrays.asList(deployment.getSpec().getDeploymentSettings().getName()))
          .reduce(new ArrayList<>(), (a, b) -> {
            a.addAll(b);
            return a;
          });
      return pods.stream().filter(pod -> validPodNames.contains(pod.getMetadata().getLabels().get("app-selector"))).map(pod -> new K8sPodDTO(pod)).collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  /**
   * Container list list.
   *
   * @param name      the name
   * @param namespace the namespace
   * @return the list
   */
  @GetMapping("/{namespace}/{name}/")
  @UserAuthorized("pod.get")
  public List<Container> containerList(
      @PathVariable("name") String name,
      @PathVariable("namespace") String namespace
  ) {
    List<Container> returnList = new ArrayList<>();
    Pod pod = k8sService.getClient().pods().inNamespace(namespace).withName(name).get();
    if (pod != null) {
      returnList.addAll(pod.getSpec().getContainers());
    }
    return returnList;
  }

  /**
   * Gets log.
   *
   * @param name      the name
   * @param namespace the namespace
   * @param container the container
   * @return the log
   */
  @GetMapping("/{namespace}/{name}/{container}/logs")
  @UserAuthorized("pod.log")
  public String getLog(
      @PathVariable("name") String name,
      @PathVariable("namespace") String namespace,
      @PathVariable("container") String container
  ) {
    return k8sService.getClient().pods().inNamespace(namespace).withName(name).inContainer(container).getLog(true);
  }

  /**
   * Gets log follow.
   *
   * @param name      the name
   * @param namespace the namespace
   * @param container the container
   * @param response  the response
   */
  @GetMapping("/{namespace}/{name}/{container}/logs/follow")
  @UserAuthorized("pod.log.follow")
  public void getLogFollow(
      @PathVariable("name") String name,
      @PathVariable("namespace") String namespace,
      @PathVariable("container") String container,
      HttpServletResponse response
  ) {
    LogWatch logWatch = k8sService.getClient().pods().inNamespace(namespace).withName(name).inContainer(container).watchLog();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(logWatch.getOutput()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        response.getWriter().write(line);
        response.flushBuffer();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
