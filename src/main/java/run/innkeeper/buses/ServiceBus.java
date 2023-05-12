package run.innkeeper.buses;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceFluent;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.guest.crd.objects.service.ServicePort;

import java.util.*;
import java.util.stream.Collectors;

public class ServiceBus {
    public static ServiceBus serviceBus = new ServiceBus();
    K8sService k8sService = K8sService.get();

    public static ServiceBus get() {
        return serviceBus;
    }

    public Service createService(ServiceSettings serviceSettings) {
        Service service = buildService(serviceSettings);
        return k8sService.getClient().resource(service).create();
    }

    public Service getService(ServiceSettings serviceSettings) {
        return k8sService
            .getClient()
            .services()
            .resource(
                new ServiceBuilder()
                    .withNewMetadata()
                        .withName(serviceSettings.getName())
                        .withNamespace(serviceSettings.getNamespace())
                    .endMetadata()
                .build()
            ).get();
    }

    public Service updateService(ServiceSettings serviceSettings) {
        Service service = buildService(serviceSettings);
        return k8sService.getClient().resource(service).patch();
    }

    public void deleteService(ServiceSettings serviceSettings) {
        k8sService
            .getClient()
            .services()
            .resource(
                new ServiceBuilder()
                    .withNewMetadata()
                        .withName(serviceSettings.getName())
                        .withNamespace(serviceSettings.getNamespace())
                    .endMetadata()
                    .build()
            )
            .delete();
    }

    private Service buildService(ServiceSettings serviceSettings) {
        Map<String, String> labels = new HashMap<>();
        labels.put("app-selector", serviceSettings.getDeployment());
        ServiceFluent.SpecNested<ServiceBuilder> sb = new ServiceBuilder()
            .withNewMetadata()
            .withName(serviceSettings.getName())
            .withNamespace(serviceSettings.getNamespace())
            .endMetadata()
            .withNewSpec()
            .withSelector(labels)
            .withType(serviceSettings.getType()).addAllToPorts(serviceSettings.getPorts().stream().map(port->new ServicePortBuilder()
                .withName(port.getName())
                .withProtocol(port.getProtocol())
                .withPort(port.getPort())
                .withTargetPort(port.getTargetPort())
                .build()).collect(Collectors.toList()));
        return sb.endSpec().build();
    }
}
