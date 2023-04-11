package run.innkeeper.v1.guest.crd.objects.deployment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Container extends io.fabric8.kubernetes.api.model.Container {
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
