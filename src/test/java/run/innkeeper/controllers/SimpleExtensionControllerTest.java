package run.innkeeper.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.buses.ExtensionBus;
import run.innkeeper.events.extension.ExtensionCheck;
import run.innkeeper.events.extension.ExtensionCreate;
import run.innkeeper.events.extension.ExtensionDelete;
import run.innkeeper.events.extension.ExtensionUpdate;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionSpec;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionState;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionStatus;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleExtensionControllerTest {

    @Mock
    ExtensionBus extensionBus;

    SimpleExtensionController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new SimpleExtensionController();
        Field field = SimpleExtensionController.class.getDeclaredField("extensionBus");
        field.setAccessible(true);
        field.set(controller, extensionBus);
    }

    private SimpleExtension createExtension() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("test-ns", "test-ext");
        SimpleExtensionSpec spec = new SimpleExtensionSpec();
        spec.setType("BackendProxy");
        spec.setName("test-ext");
        se.setSpec(spec);
        se.setStatus(new SimpleExtensionStatus());
        return se;
    }

    @Test
    void create_setsState_whenNotNull() {
        SimpleExtension se = createExtension();
        when(extensionBus.create(se)).thenReturn(SimpleExtensionState.UP_TO_DATE);

        controller.create(new ExtensionCreate(se));

        assertEquals(SimpleExtensionState.UP_TO_DATE, se.getStatus().getCurrentState());
    }

    @Test
    void create_doesNotSetState_whenNull() {
        SimpleExtension se = createExtension();
        when(extensionBus.create(se)).thenReturn(null);

        controller.create(new ExtensionCreate(se));

        assertEquals(SimpleExtensionState.NEED_TO_CREATE, se.getStatus().getCurrentState());
    }

    @Test
    void update_setsState_whenNotNull() {
        SimpleExtension se = createExtension();
        when(extensionBus.update(se)).thenReturn(SimpleExtensionState.UP_TO_DATE);

        controller.update(new ExtensionUpdate(se));

        assertEquals(SimpleExtensionState.UP_TO_DATE, se.getStatus().getCurrentState());
    }

    @Test
    void update_doesNotSetState_whenNull() {
        SimpleExtension se = createExtension();
        when(extensionBus.update(se)).thenReturn(null);

        controller.update(new ExtensionUpdate(se));

        assertEquals(SimpleExtensionState.NEED_TO_CREATE, se.getStatus().getCurrentState());
    }

    @Test
    void check_setsState_whenNotNull() {
        SimpleExtension se = createExtension();
        when(extensionBus.check(se)).thenReturn(SimpleExtensionState.NEED_TO_UPDATE);

        controller.check(new ExtensionCheck(se));

        assertEquals(SimpleExtensionState.NEED_TO_UPDATE, se.getStatus().getCurrentState());
    }

    @Test
    void check_doesNotSetState_whenNull() {
        SimpleExtension se = createExtension();
        when(extensionBus.check(se)).thenReturn(null);

        controller.check(new ExtensionCheck(se));

        assertEquals(SimpleExtensionState.NEED_TO_CREATE, se.getStatus().getCurrentState());
    }

    @Test
    void delete_callsExtensionBus() {
        SimpleExtension se = createExtension();

        controller.delete(new ExtensionDelete(se));

        verify(extensionBus).delete(se);
    }
}
