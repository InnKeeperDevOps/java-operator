package run.innkeeper.v1.guest.crd.objects;

import io.fabric8.generator.annotation.Required;

public class ServiceSettings {
    @Required
    String deployment;
    @Required
    int port;
    @Required
    String name;

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
