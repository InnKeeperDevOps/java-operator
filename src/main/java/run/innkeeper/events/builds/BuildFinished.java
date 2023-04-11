package run.innkeeper.events.builds;

import run.innkeeper.events.structure.BuildEvent;
import run.innkeeper.v1.build.crd.Build;

public class BuildFinished extends BuildEvent {

    public BuildFinished(Build build) {
        super(build);
    }
}
