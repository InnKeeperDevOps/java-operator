package run.innkeeper.events.actions.builds;

import run.innkeeper.events.structure.BuildEvent;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;

public class UpdateBuild extends BuildEvent {
    BuildSettings newBuildSettings;

    public UpdateBuild(Build buildObj, BuildSettings newBuildSettings) {
        super(buildObj);
        this.newBuildSettings = newBuildSettings;
    }

    public BuildSettings getNewBuild() {
        return newBuildSettings;
    }
}
