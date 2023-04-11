package run.innkeeper.events.actions.builds;

import run.innkeeper.events.structure.BuildEvent;
import run.innkeeper.v1.build.crd.Build;

public class MonitorBuild extends BuildEvent {
    public MonitorBuild(Build build) {
        super(build);
    }
}
