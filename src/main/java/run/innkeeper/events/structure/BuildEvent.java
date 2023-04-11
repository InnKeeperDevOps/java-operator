package run.innkeeper.events.structure;

import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.guest.crd.Guest;

public class BuildEvent extends Event {
    Build build;

    public BuildEvent(Build build) {
        super();
        this.build = build;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }
}
