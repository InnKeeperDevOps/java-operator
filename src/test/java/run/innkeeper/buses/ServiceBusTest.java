package run.innkeeper.buses;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.guest.crd.objects.service.ServicePort;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceBusTest {

    @Mock
    K8sService k8sService;

    @Mock
    KubernetesClient client;

    ServiceBus serviceBus;

    @BeforeEach
    void setUp() throws Exception {
        serviceBus = new ServiceBus();
        Field k8sField = ServiceBus.class.getDeclaredField("k8sService");
        k8sField.setAccessible(true);
        k8sField.set(serviceBus, k8sService);
        lenient().when(k8sService.getClient()).thenReturn(client);
    }

    private ServiceSettings createServiceSettings() {
        ServiceSettings ss = new ServiceSettings();
        ss.setName("test-svc");
        ss.setNamespace("test-ns");
        ss.setDeployment("test-dep");
        ss.setType("ClusterIP");
        ServicePort port = new ServicePort();
        port.setName("http");
        port.setProtocol("TCP");
        port.setPort(80);
        port.setTargetPort(new IntOrString(8080));
        ss.setPorts(Arrays.asList(port));
        return ss;
    }

    @Test
    void get_returnsSingleton() {
        assertNotNull(ServiceBus.get());
    }

    @SuppressWarnings("unchecked")
    @Test
    void createService_buildsAndCreates() {
        ServiceSettings ss = createServiceSettings();
        io.fabric8.kubernetes.client.dsl.NamespaceableResource<Service> resource = mock(io.fabric8.kubernetes.client.dsl.NamespaceableResource.class);
        when(client.resource(any(Service.class))).thenReturn(resource);
        Service svc = new ServiceBuilder().withNewMetadata().withName("test-svc").endMetadata().build();
        when(resource.create()).thenReturn(svc);

        Service result = serviceBus.createService(ss);
        assertNotNull(result);
        verify(resource).create();
    }

    @SuppressWarnings("unchecked")
    @Test
    void updateService_buildsAndPatches() {
        ServiceSettings ss = createServiceSettings();
        io.fabric8.kubernetes.client.dsl.NamespaceableResource<Service> resource = mock(io.fabric8.kubernetes.client.dsl.NamespaceableResource.class);
        when(client.resource(any(Service.class))).thenReturn(resource);
        Service svc = new ServiceBuilder().withNewMetadata().withName("test-svc").endMetadata().build();
        when(resource.patch()).thenReturn(svc);

        Service result = serviceBus.updateService(ss);
        assertNotNull(result);
        verify(resource).patch();
    }

    @SuppressWarnings("unchecked")
    @Test
    void getService_queriesK8s() {
        ServiceSettings ss = createServiceSettings();
        MixedOperation svcOps = mock(MixedOperation.class);
        when(client.services()).thenReturn(svcOps);
        ServiceResource resource = mock(ServiceResource.class);
        when(svcOps.resource(any(Service.class))).thenReturn(resource);
        when(resource.get()).thenReturn(null);

        Service result = serviceBus.getService(ss);
        assertNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void deleteService_callsDelete() {
        ServiceSettings ss = createServiceSettings();
        MixedOperation svcOps = mock(MixedOperation.class);
        when(client.services()).thenReturn(svcOps);
        ServiceResource resource = mock(ServiceResource.class);
        when(svcOps.resource(any(Service.class))).thenReturn(resource);

        serviceBus.deleteService(ss);
        verify(resource).delete();
    }
}
