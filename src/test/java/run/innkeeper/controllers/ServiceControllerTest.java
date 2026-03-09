package run.innkeeper.controllers;

import io.fabric8.kubernetes.api.model.IntOrString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.buses.ServiceBus;
import run.innkeeper.events.actions.services.CreateService;
import run.innkeeper.events.actions.services.UpdateService;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.guest.crd.objects.service.ServicePort;
import run.innkeeper.v1.service.crd.Service;
import run.innkeeper.v1.service.crd.ServiceSpec;
import run.innkeeper.v1.service.crd.ServiceState;
import run.innkeeper.v1.service.crd.ServiceStatus;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceControllerTest {

    @Mock
    ServiceBus serviceBus;

    ServiceController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new ServiceController();
        Field field = ServiceController.class.getDeclaredField("serviceBus");
        field.setAccessible(true);
        field.set(controller, serviceBus);
    }

    private Service createService() {
        Service svc = new Service();
        svc.setMetaData("test-ns", "test-svc");
        ServiceSpec spec = new ServiceSpec();
        ServiceSettings ss = new ServiceSettings();
        ss.setName("test-svc");
        ss.setNamespace("test-ns");
        ss.setDeployment("test-dep");
        ss.setType("ClusterIP");
        ServicePort port = new ServicePort();
        port.setName("http");
        port.setPort(80);
        port.setProtocol("TCP");
        port.setTargetPort(new IntOrString(8080));
        ss.setPorts(Arrays.asList(port));
        spec.setServiceSettings(ss);
        svc.setSpec(spec);
        svc.setStatus(new ServiceStatus());
        return svc;
    }

    @Test
    void createService_whenServiceNotExists_creates() {
        Service svc = createService();
        when(serviceBus.getService(svc.getSpec().getServiceSettings())).thenReturn(null);

        controller.createService(new CreateService(svc));

        verify(serviceBus).createService(svc.getSpec().getServiceSettings());
        assertEquals(ServiceState.CREATED, svc.getStatus().getServiceState());
    }

    @Test
    void createService_whenServiceExists_doesNotCreate() {
        Service svc = createService();
        when(serviceBus.getService(svc.getSpec().getServiceSettings()))
            .thenReturn(new io.fabric8.kubernetes.api.model.Service());

        controller.createService(new CreateService(svc));

        verify(serviceBus, never()).createService(any());
    }

    @Test
    void updateService_whenServiceExists_updates() {
        Service svc = createService();
        when(serviceBus.getService(svc.getSpec().getServiceSettings()))
            .thenReturn(new io.fabric8.kubernetes.api.model.Service());

        controller.updateService(new UpdateService(svc));

        verify(serviceBus).updateService(svc.getSpec().getServiceSettings());
        assertEquals(ServiceState.CREATED, svc.getStatus().getServiceState());
    }

    @Test
    void updateService_whenServiceNotExists_doesNotUpdate() {
        Service svc = createService();
        when(serviceBus.getService(svc.getSpec().getServiceSettings())).thenReturn(null);

        controller.updateService(new UpdateService(svc));

        verify(serviceBus, never()).updateService(any());
    }
}
