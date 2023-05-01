package run.innkeeper.v1.guest.crd.objects.service;

import io.fabric8.generator.annotation.Required;
import io.fabric8.kubernetes.api.model.IntOrString;

public class ServicePort {
    @Required
    String name;
    @Required
    String protocol;

    @Required
    int port;

    @Required
    IntOrString targetPort;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public IntOrString getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(IntOrString targetPort) {
        this.targetPort = targetPort;
    }
}
