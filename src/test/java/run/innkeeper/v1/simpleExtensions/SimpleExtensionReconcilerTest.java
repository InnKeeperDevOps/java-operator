package run.innkeeper.v1.simpleExtensions;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.buses.EventBus;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionSpec;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionState;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionStatus;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleExtensionReconcilerTest {

    @Mock
    EventBus eventBus;

    @Mock
    K8sService k8sService;

    @Mock
    Context<SimpleExtension> context;

    SimpleExtensionReconciler reconciler;

    @BeforeEach
    void setUp() throws Exception {
        reconciler = new SimpleExtensionReconciler();
        Field eventBusField = SimpleExtensionReconciler.class.getDeclaredField("eventBus");
        eventBusField.setAccessible(true);
        eventBusField.set(reconciler, eventBus);
        Field k8sField = SimpleExtensionReconciler.class.getDeclaredField("k8sService");
        k8sField.setAccessible(true);
        k8sField.set(reconciler, k8sService);
    }

    private SimpleExtension createExtension(SimpleExtensionState state) {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("test-ns", "test-ext");
        SimpleExtensionSpec spec = new SimpleExtensionSpec();
        spec.setType("BackendProxy");
        spec.setName("test-ext");
        se.setSpec(spec);
        if (state != null) {
            se.setStatus(new SimpleExtensionStatus());
            se.getStatus().setCurrentState(state);
        }
        return se;
    }

    @Test
    void reconcile_nullStatus_initializesStatus() throws Exception {
        SimpleExtension se = createExtension(null);

        UpdateControl<SimpleExtension> result = reconciler.reconcile(se, context);

        assertNotNull(se.getStatus());
        assertEquals(SimpleExtensionState.NEED_TO_CREATE, se.getStatus().getCurrentState());
    }

    @Test
    void reconcile_needToUpdateState_doesNotThrow() throws Exception {
        SimpleExtension se = createExtension(SimpleExtensionState.NEED_TO_UPDATE);
        assertDoesNotThrow(() -> reconciler.reconcile(se, context));
    }

    @Test
    void reconcile_needToCreateState_doesNotThrow() throws Exception {
        SimpleExtension se = createExtension(SimpleExtensionState.NEED_TO_CREATE);
        assertDoesNotThrow(() -> reconciler.reconcile(se, context));
    }

    @Test
    void reconcile_upToDateState_doesNotThrow() throws Exception {
        SimpleExtension se = createExtension(SimpleExtensionState.UP_TO_DATE);
        assertDoesNotThrow(() -> reconciler.reconcile(se, context));
    }

    @Test
    void cleanup_firesDeleteEvent() {
        SimpleExtension se = createExtension(SimpleExtensionState.UP_TO_DATE);

        DeleteControl result = reconciler.cleanup(se, context);

        verify(eventBus).fire(any());
        assertNotNull(result);
    }
}
