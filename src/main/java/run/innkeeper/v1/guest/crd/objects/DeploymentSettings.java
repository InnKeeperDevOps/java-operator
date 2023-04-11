package run.innkeeper.v1.guest.crd.objects;

import run.innkeeper.v1.guest.crd.objects.deployment.Container;
import io.fabric8.generator.annotation.Required;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;

public class DeploymentSettings {
    EnvVar[] env;
    VolumeMount[] mounts;
    Volume[] volumes;
    @Required
    String namespace;
    @Required
    String name;

    @Required
    Container[] containers;

    public EnvVar[] getEnv() {
        return env;
    }

    public void setEnv(EnvVar[] env) {
        this.env = env;
    }

    public VolumeMount[] getMounts() {
        return mounts;
    }

    public void setMounts(VolumeMount[] mounts) {
        this.mounts = mounts;
    }

    public Volume[] getVolumes() {
        return volumes;
    }

    public void setVolumes(Volume[] volumes) {
        this.volumes = volumes;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Container[] getContainers() {
        return containers;
    }

    public void setContainers(Container[] containers) {
        this.containers = containers;
    }
}
