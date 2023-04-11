package run.innkeeper.events.builds;

import run.innkeeper.events.structure.BuildEvent;
import run.innkeeper.v1.build.crd.Build;

public class BuildStarted extends BuildEvent {
    public BuildStarted(Build build) {
        super(build);
    }
}
