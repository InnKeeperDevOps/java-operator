package run.innkeeper.events.actions.builds;

import run.innkeeper.events.structure.BuildEvent;
import run.innkeeper.v1.build.crd.Build;

public class StartBuild extends BuildEvent {
    public StartBuild(Build build) {
        super(build);
    }

}
