package run.innkeeper.events.builds;

import run.innkeeper.events.structure.Event;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;

public class CreateBuild extends Event {
    BuildSettings buildSettings;
    public CreateBuild(BuildSettings buildSettings) {
        this.buildSettings = buildSettings;
    }

    public BuildSettings getBuild() {
        return buildSettings;
    }

    public void setBuild(BuildSettings buildSettings) {
        this.buildSettings = buildSettings;
    }
}
