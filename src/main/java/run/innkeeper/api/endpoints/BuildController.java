package run.innkeeper.api.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.auth.UserAuthorized;
import run.innkeeper.api.dto.BuildDTO;
import run.innkeeper.api.dto.DeploymentDTO;
import run.innkeeper.services.K8sService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/build")
public class BuildController{
  K8sService k8sService = K8sService.get();
  @GetMapping("/")
  @UserAuthorized("build.list")
  public List<BuildDTO> getBuilds() {
    return k8sService
        .getBuildClient()
        .list()
        .getItems()
        .stream()
        .map(d -> new BuildDTO(d))
        .collect(Collectors.toList());
  }
}
