package run.innkeeper.events.actions.builds;

import run.innkeeper.events.structure.BuildEvent;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;

import java.util.List;

public class UpdateBuild extends BuildEvent {
    List<String> changes;
    BuildSettings newBuildSettings;

    public UpdateBuild(Build buildObj, BuildSettings newBuildSettings, List<String> changes) {
        super(buildObj);
        this.changes = changes;
        this.newBuildSettings = newBuildSettings;
    }

    public List<String> getChanges() {
        return changes;
    }

    public BuildSettings getNewBuild() {
        return newBuildSettings;
    }
}
