package run.innkeeper.api.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.auth.UserAuthorized;
import run.innkeeper.api.dto.ServiceDTO;
import run.innkeeper.api.dto.SimpleExtensionDTO;
import run.innkeeper.buses.ExtensionBus;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/extension")
public class ExtensionController{
  K8sService k8sService = K8sService.get();

  @GetMapping("/")
  @UserAuthorized("extension.list")
  public List<SimpleExtensionDTO> getExtensions() {
    return k8sService
        .getSimpleExtensionClient()
        .list()
        .getItems()
        .stream()
        .map(d -> new SimpleExtensionDTO(d))
        .collect(Collectors.toList());
  }

  @GetMapping("/{namespace}/{name}/")
  @UserAuthorized("extension.get")
  public SimpleExtensionDTO getExtension(@PathVariable String name, @PathVariable String namespace) {
    return new SimpleExtensionDTO(
        k8sService
            .getSimpleExtensionClient()
            .inNamespace(namespace)
            .withName(name)
            .get()
    );
  }
  @GetMapping("/{namespace}/{name}/object")
  @UserAuthorized("extension.k8s")
  public Object getExtensionObject(@PathVariable String name, @PathVariable String namespace) {
    SimpleExtension simpleExtension = k8sService
        .getSimpleExtensionClient()
        .inNamespace(namespace)
        .withName(name)
        .get();
    return ExtensionBus.getExtensionBus().get(simpleExtension).get(simpleExtension);
  }
}
