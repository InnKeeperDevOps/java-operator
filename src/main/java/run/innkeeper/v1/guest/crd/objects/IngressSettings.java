package run.innkeeper.v1.guest.crd.objects;

import io.fabric8.generator.annotation.Required;
import run.innkeeper.v1.guest.crd.objects.ingress.IngressType;

import java.util.HashMap;
import java.util.Map;

public class IngressSettings {
    @Required
    IngressType type;

    @Required
    String name;
    @Required
    String domain;
    @Required
    String path;
    Map<String, String> headers = new HashMap<>();
    @Required
    String[] services;
    @Required
    String[] gateways;
    @Required
    int port;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public IngressType getType() {
        return type;
    }

    public void setType(IngressType type) {
        this.type = type;
    }

    public String[] getGateways() {
        return gateways;
    }

    public void setGateways(String[] gateways) {
        this.gateways = gateways;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String[] getServices() {
        return services;
    }

    public void setServices(String[] services) {
        this.services = services;
    }
}
