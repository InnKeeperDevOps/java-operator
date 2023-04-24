package run.innkeeper.v1.guest.crd.objects.deployment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.generator.annotation.Required;
import io.fabric8.kubernetes.api.model.ContainerPort;

import java.util.List;

public class Container extends io.fabric8.kubernetes.api.model.Container {
    @Required
    String buildName;

    public Container() {
    }
    public String getBuildName() {
        return buildName;
    }
    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

}
