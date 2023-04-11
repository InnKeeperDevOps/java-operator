package run.innkeeper.controllers.build;

import run.innkeeper.events.structure.Trigger;
import run.innkeeper.events.builds.BuildCheck;

public class CheckIfBuildUpdated {
    @Trigger(BuildCheck.class)
    public void checkIfUpdated(BuildCheck event){

    }
}
