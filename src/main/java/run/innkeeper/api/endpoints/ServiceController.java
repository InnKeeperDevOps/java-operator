package run.innkeeper.api.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.auth.UserAuthorized;
import run.innkeeper.api.dto.ServiceDTO;
import run.innkeeper.api.dto.k8s.K8sServiceDTO;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.service.crd.Service;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/service")
public class ServiceController{
  K8sService k8sService = K8sService.get();

  @GetMapping("/")
  @UserAuthorized("service.log.follow")
  public List<ServiceDTO> getServices() {
    return k8sService
        .getServiceClient()
        .list()
        .getItems()
        .stream()
        .map(d -> new ServiceDTO(d))
        .collect(Collectors.toList());
  }

  @GetMapping("/{namespace}/{name}/")
  @UserAuthorized("service.get")
  public ServiceDTO getService(@PathVariable String name, @PathVariable String namespace) {
    return new ServiceDTO(k8sService
                              .getServiceClient()
                              .inNamespace(namespace)
                              .withName(name)
                              .get());
  }

  @GetMapping("/{namespace}/{name}/object")
  @UserAuthorized("service.k8s")
  public K8sServiceDTO getK8sService(@PathVariable String name, @PathVariable String namespace) {
    Service service = k8sService
        .getServiceClient()
        .inNamespace(namespace)
        .withName(name)
        .get();
    ServiceSettings serviceSettings = service.getSpec().getServiceSettings();
    return new K8sServiceDTO(
        k8sService
            .getClient()
            .services()
            .inNamespace(serviceSettings.getNamespace())
            .withName(serviceSettings.getName())
            .get()
    );
  }

}
