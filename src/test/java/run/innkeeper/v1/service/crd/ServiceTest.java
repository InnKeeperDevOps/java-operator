package run.innkeeper.v1.service.crd;

import org.junit.jupiter.api.Test;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.guest.crd.objects.service.ServicePort;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTest {

    @Test
    void setMetaData_setsNameAndNamespace() {
        Service svc = new Service();
        svc.setMetaData("test-ns", "test-name");
        assertEquals("test-ns", svc.getMetadata().getNamespace());
        assertEquals("test-name", svc.getMetadata().getName());
    }

    @Test
    void serviceSpec_getSetServiceSettings() {
        ServiceSpec spec = new ServiceSpec();
        ServiceSettings ss = new ServiceSettings();
        ss.setName("svc1");
        ss.setNamespace("ns1");
        ss.setDeployment("dep1");
        spec.setServiceSettings(ss);
        assertEquals("svc1", spec.getServiceSettings().getName());
        assertEquals("dep1", spec.getServiceSettings().getDeployment());
        // setType only sets if current type is not null, so it's null on new instance
        assertNull(spec.getServiceSettings().getType());
    }

    @Test
    void serviceStatus_getSetState() {
        ServiceStatus status = new ServiceStatus();
        status.setServiceState(ServiceState.CREATED);
        assertEquals(ServiceState.CREATED, status.getServiceState());
    }

    @Test
    void serviceState_allValues() {
        assertEquals(5, ServiceState.values().length);
        assertNotNull(ServiceState.valueOf("CREATING"));
        assertNotNull(ServiceState.valueOf("CREATED"));
        assertNotNull(ServiceState.valueOf("NEED_TO_CREATE"));
        assertNotNull(ServiceState.valueOf("RECREATE"));
        assertNotNull(ServiceState.valueOf("FAILED"));
    }

    @Test
    void servicePort_allFields() {
        ServicePort port = new ServicePort();
        port.setName("http");
        port.setProtocol("TCP");
        port.setPort(80);
        port.setTargetPort(new io.fabric8.kubernetes.api.model.IntOrString(8080));

        assertEquals("http", port.getName());
        assertEquals("TCP", port.getProtocol());
        assertEquals(80, port.getPort());
        assertEquals(8080, port.getTargetPort().getIntVal());
    }

    @Test
    void serviceSettings_ports() {
        ServiceSettings ss = new ServiceSettings();
        ServicePort port = new ServicePort();
        port.setName("http");
        port.setPort(80);
        ss.setPorts(Arrays.asList(port));
        assertEquals(1, ss.getPorts().size());
    }
}
