package run.innkeeper.events.builds;

import run.innkeeper.events.structure.BuildEvent;
import run.innkeeper.v1.build.crd.Build;

public class BuildCheck extends BuildEvent {
    public BuildCheck(Build build) {
        super(build);
    }
}
