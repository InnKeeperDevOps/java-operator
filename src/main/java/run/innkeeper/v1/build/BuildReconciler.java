package run.innkeeper.v1.build;

import io.javaoperatorsdk.operator.api.reconciler.*;
import run.innkeeper.buses.EventBus;
import run.innkeeper.events.actions.builds.CheckGitBuild;
import run.innkeeper.events.actions.builds.MonitorBuild;
import run.innkeeper.events.actions.builds.MonitorGit;
import run.innkeeper.events.actions.builds.StartBuild;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.build.crd.BuildState;
import run.innkeeper.v1.build.crd.BuildStatus;

import java.util.concurrent.TimeUnit;

@ControllerConfiguration(maxReconciliationInterval = @MaxReconciliationInterval(
    interval = 15,
    timeUnit = TimeUnit.SECONDS))
public class BuildReconciler implements Reconciler<Build> {

    EventBus eventBus = EventBus.get();
    @Override
    public UpdateControl<Build> reconcile(Build build, Context<Build> context) throws Exception {
        Logging.debug("================== BUILD RECONCILE =========================");
        if(build.getStatus() == null){
            build.setStatus(new BuildStatus());
            build.getStatus().setState(BuildState.WAITING);
        }else {
            switch (build.getStatus().getState()) {
                case WAITING -> eventBus.fire(new CheckGitBuild(build));
                case BUILDING -> eventBus.fire(new MonitorBuild(build));
                case GIT_CHECK -> eventBus.fire(new MonitorGit(build));
                case NEED_TO_BUILD -> EventBus.get().fire(new StartBuild(build));
            }
        }
        return UpdateControl.patchStatus(build);
    }
    // Return the changed resource, so it gets updated. See javadoc for details.

}