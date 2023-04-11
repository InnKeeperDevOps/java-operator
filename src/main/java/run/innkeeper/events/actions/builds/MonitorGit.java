package run.innkeeper.events.actions.builds;

import run.innkeeper.events.structure.BuildEvent;
import run.innkeeper.v1.build.crd.Build;

public class MonitorGit extends BuildEvent {
    public MonitorGit(Build build) {
        super(build);
    }
}
