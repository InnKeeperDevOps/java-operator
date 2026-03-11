package run.innkeeper.buses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import run.innkeeper.extensions.ExtensionStructure;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionSpec;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionState;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionBusTest {

    ExtensionBus extensionBus;

    @BeforeEach
    void setUp() {
        extensionBus = new ExtensionBus();
    }

    @Test
    void getExtensionBus_returnsSingleton() {
        assertNotNull(ExtensionBus.getExtensionBus());
    }

    @Test
    void init_registersExtensions() {
        assertDoesNotThrow(() -> extensionBus.init());
    }

    @Test
    void get_unknownType_returnsNull() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("ns", "test");
        SimpleExtensionSpec spec = new SimpleExtensionSpec();
        spec.setType("UnknownType");
        se.setSpec(spec);

        ExtensionStructure result = extensionBus.get(se);
        assertNull(result);
    }

    @Test
    void create_unknownType_returnsNull() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("ns", "test");
        SimpleExtensionSpec spec = new SimpleExtensionSpec();
        spec.setType("UnknownType");
        se.setSpec(spec);

        SimpleExtensionState result = extensionBus.create(se);
        assertNull(result);
    }

    @Test
    void update_unknownType_returnsNull() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("ns", "test");
        SimpleExtensionSpec spec = new SimpleExtensionSpec();
        spec.setType("UnknownType");
        se.setSpec(spec);

        SimpleExtensionState result = extensionBus.update(se);
        assertNull(result);
    }

    @Test
    void delete_unknownType_doesNotThrow() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("ns", "test");
        SimpleExtensionSpec spec = new SimpleExtensionSpec();
        spec.setType("UnknownType");
        se.setSpec(spec);

        assertDoesNotThrow(() -> extensionBus.delete(se));
    }

    @Test
    void check_unknownType_returnsNull() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("ns", "test");
        SimpleExtensionSpec spec = new SimpleExtensionSpec();
        spec.setType("UnknownType");
        se.setSpec(spec);

        SimpleExtensionState result = extensionBus.check(se);
        assertNull(result);
    }

    @Test
    void init_thenGet_backendProxy() {
        extensionBus.init();
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("ns", "test");
        SimpleExtensionSpec spec = new SimpleExtensionSpec();
        spec.setType("BackendProxy");
        se.setSpec(spec);

        ExtensionStructure result = extensionBus.get(se);
        assertNotNull(result);
    }
}
