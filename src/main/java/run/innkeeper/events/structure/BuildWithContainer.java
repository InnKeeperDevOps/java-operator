package run.innkeeper.events.structure;

import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.guest.crd.objects.deployment.Container;

public class BuildWithContainer {
    Build build;
    Container container;
    public BuildWithContainer(Build build, Container container) {
        this.build = build;
        this.container = container;
    }
    public Build getBuild() {
        return build;
    }
    public Container getContainer() {
        return container;
    }
}