package run.innkeeper.v1.simpleExtensions.crd;

import io.fabric8.kubernetes.api.model.IntOrString;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SimpleExtensionTest {

    @Test
    void setMetaData_setsNameAndNamespace() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("test-ns", "test-name");
        assertEquals("test-ns", se.getMetadata().getNamespace());
        assertEquals("test-name", se.getMetadata().getName());
    }

    @Test
    void simpleExtensionSpec_getSetType() {
        SimpleExtensionSpec spec = new SimpleExtensionSpec();
        spec.setType("BackendProxy");
        assertEquals("BackendProxy", spec.getType());
    }

    @Test
    void simpleExtensionSpec_getSetName() {
        SimpleExtensionSpec spec = new SimpleExtensionSpec();
        spec.setName("my-extension");
        assertEquals("my-extension", spec.getName());
    }

    @Test
    void simpleExtensionSpec_getSetData() {
        SimpleExtensionSpec spec = new SimpleExtensionSpec();
        Map<String, IntOrString> data = new HashMap<>();
        data.put("port", new IntOrString(8080));
        data.put("host", new IntOrString("localhost"));
        spec.setData(data);
        assertEquals(2, spec.getData().size());
        assertEquals(8080, spec.getData().get("port").getIntVal());
    }

    @Test
    void simpleExtensionStatus_defaultState() {
        SimpleExtensionStatus status = new SimpleExtensionStatus();
        assertEquals(SimpleExtensionState.NEED_TO_CREATE, status.getCurrentState());
    }

    @Test
    void simpleExtensionStatus_getSetState() {
        SimpleExtensionStatus status = new SimpleExtensionStatus();
        status.setCurrentState(SimpleExtensionState.UP_TO_DATE);
        assertEquals(SimpleExtensionState.UP_TO_DATE, status.getCurrentState());
    }

    @Test
    void simpleExtensionState_allValues() {
        SimpleExtensionState[] values = SimpleExtensionState.values();
        assertTrue(values.length >= 7);
        assertNotNull(SimpleExtensionState.valueOf("UP_TO_DATE"));
        assertNotNull(SimpleExtensionState.valueOf("NEED_TO_UPDATE"));
        assertNotNull(SimpleExtensionState.valueOf("NEED_TO_CREATE"));
    }

    @Test
    void backendProxySettings_fromSimpleExtension() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("test-ns", "test-name");
        SimpleExtensionSpec spec = new SimpleExtensionSpec();
        spec.setType("BackendProxy");
        Map<String, IntOrString> data = new HashMap<>();
        data.put("backendUri", new IntOrString("https://proxy.example.com"));
        data.put("backendToken", new IntOrString("token123"));
        data.put("service", new IntOrString("my-service"));
        data.put("namespace", new IntOrString("my-ns"));
        data.put("servicePort", new IntOrString(8080));
        data.put("port", new IntOrString(9090));
        data.put("ip", new IntOrString("10.0.0.1"));
        spec.setData(data);
        se.setSpec(spec);

        BackendProxySettings bps = new BackendProxySettings(se);
        assertEquals("https://proxy.example.com", bps.getBackendUri());
        assertEquals("token123", bps.getBackendToken());
        assertEquals("my-service", bps.getService());
        assertEquals("my-ns", bps.getNamespace());
        assertEquals(8080, bps.getServicePort().getIntVal());
        assertEquals(9090, bps.getPort().getIntVal());
        assertEquals("10.0.0.1", bps.getIp());
    }
}
