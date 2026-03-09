package run.innkeeper.api.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.annotations.UserAuthorized;
import run.innkeeper.services.AccountService;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.deployment.crd.Deployment;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/oauth/stats", "/token/stats"})
public class StatsController {
  K8sService k8sService = K8sService.get();
  AccountService accountService = AccountService.get();

  @GetMapping("/")
  @UserAuthorized("stats.view")
  public Map<String, Integer> getStats() {
    Map<String, Integer> stats = new LinkedHashMap<>();
    try {
      stats.put("guests", k8sService.getGuestClient().inAnyNamespace().list().getItems().size());
    } catch (Exception e) {
      stats.put("guests", 0);
    }
    try {
      stats.put("deployments", k8sService.getDeploymentClient().list().getItems().size());
    } catch (Exception e) {
      stats.put("deployments", 0);
    }
    try {
      stats.put("services", k8sService.getServiceClient().list().getItems().size());
    } catch (Exception e) {
      stats.put("services", 0);
    }
    try {
      List<Deployment> deployments = k8sService.getDeploymentClient().list().getItems();
      List<String> validPodNames = deployments
          .stream()
          .map(deployment -> Arrays.asList(deployment.getSpec().getDeploymentSettings().getName()))
          .reduce(new ArrayList<>(), (a, b) -> {
            a.addAll(b);
            return a;
          });
      long podCount = k8sService.getClient().pods().inAnyNamespace().list().getItems()
          .stream()
          .filter(pod -> pod.getMetadata().getLabels() != null
              && validPodNames.contains(pod.getMetadata().getLabels().get("app-selector")))
          .count();
      stats.put("pods", (int) podCount);
    } catch (Exception e) {
      stats.put("pods", 0);
    }
    try {
      stats.put("builds", k8sService.getBuildClient().list().getItems().size());
    } catch (Exception e) {
      stats.put("builds", 0);
    }
    try {
      stats.put("extensions", k8sService.getSimpleExtensionClient().list().getItems().size());
    } catch (Exception e) {
      stats.put("extensions", 0);
    }
    try {
      stats.put("accounts", accountService.countAccounts());
    } catch (Exception e) {
      stats.put("accounts", 0);
    }
    return stats;
  }
}
