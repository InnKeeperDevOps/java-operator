package run.innkeeper.v1.build.crd;

import run.innkeeper.v1.guest.crd.objects.BuildSettings;

public class BuildSpec  {
    BuildSettings buildSettings;

    public BuildSettings getBuildSettings() {
        return buildSettings;
    }

    public void setBuildSettings(BuildSettings buildSettings) {
        this.buildSettings = buildSettings;
    }
}
