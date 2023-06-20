package run.innkeeper.events.actions.builds;

import run.innkeeper.events.structure.BuildEvent;
import run.innkeeper.v1.build.crd.Build;

public class FailedBuild extends BuildEvent {
    public FailedBuild(Build build) {
        super(build);
    }

}
