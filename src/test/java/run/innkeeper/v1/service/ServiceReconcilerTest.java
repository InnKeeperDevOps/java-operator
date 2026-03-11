package run.innkeeper.v1.service;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.buses.ServiceBus;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.service.crd.Service;
import run.innkeeper.v1.service.crd.ServiceSpec;
import run.innkeeper.v1.service.crd.ServiceState;
import run.innkeeper.v1.service.crd.ServiceStatus;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceReconcilerTest {

    @Mock
    ServiceBus serviceBus;

    @Mock
    K8sService k8sService;

    @Mock
    Context<Service> context;

    ServiceReconciler reconciler;

    @BeforeEach
    void setUp() throws Exception {
        reconciler = new ServiceReconciler();
        Field sbField = ServiceReconciler.class.getDeclaredField("serviceBus");
        sbField.setAccessible(true);
        sbField.set(reconciler, serviceBus);
        Field k8sField = ServiceReconciler.class.getDeclaredField("k8sService");
        k8sField.setAccessible(true);
        k8sField.set(reconciler, k8sService);
    }

    private Service createService(ServiceState state) {
        Service svc = new Service();
        svc.setMetaData("test-ns", "test-svc");
        ServiceSpec spec = new ServiceSpec();
        ServiceSettings ss = new ServiceSettings();
        ss.setName("test-svc");
        ss.setNamespace("test-ns");
        spec.setServiceSettings(ss);
        svc.setSpec(spec);
        if (state != null) {
            svc.setStatus(new ServiceStatus());
            svc.getStatus().setServiceState(state);
        }
        return svc;
    }

    @Test
    void reconcile_nullStatus_initializesStatus() throws Exception {
        Service svc = createService(null);

        UpdateControl<Service> result = reconciler.reconcile(svc, context);

        assertNotNull(svc.getStatus());
        assertEquals(ServiceState.NEED_TO_CREATE, svc.getStatus().getServiceState());
    }

    @Test
    void reconcile_needToCreateState_doesNotThrow() throws Exception {
        Service svc = createService(ServiceState.NEED_TO_CREATE);
        assertDoesNotThrow(() -> reconciler.reconcile(svc, context));
    }

    @Test
    void reconcile_recreateState_doesNotThrow() throws Exception {
        Service svc = createService(ServiceState.RECREATE);
        assertDoesNotThrow(() -> reconciler.reconcile(svc, context));
    }

    @Test
    void cleanup_deletesService() {
        Service svc = createService(ServiceState.CREATED);

        DeleteControl result = reconciler.cleanup(svc, context);

        verify(serviceBus).deleteService(svc.getSpec().getServiceSettings());
        assertNotNull(result);
    }

    @Test
    void cleanup_handlesException() {
        Service svc = createService(ServiceState.CREATED);
        doThrow(new RuntimeException("test")).when(serviceBus).deleteService(any());

        DeleteControl result = reconciler.cleanup(svc, context);
        assertNotNull(result);
    }
}
