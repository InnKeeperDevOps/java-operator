package run.innkeeper.v1.guest.crd.objects;

import io.fabric8.generator.annotation.Required;
import run.innkeeper.v1.guest.crd.objects.service.ServicePort;

import java.util.List;

public class ServiceSettings {
    @Required
    String deployment;
    @Required
    List<ServicePort> ports;

    @Required
    String type;

    @Required
    String name;
    @Required
    String namespace;

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    public List<ServicePort> getPorts() {
        return ports;
    }

    public void setPorts(List<ServicePort> ports) {
        this.ports = ports;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
