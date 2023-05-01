package run.innkeeper.v1.build;

import io.javaoperatorsdk.operator.api.reconciler.*;
import run.innkeeper.buses.BuildBus;
import run.innkeeper.buses.EventBus;
import run.innkeeper.buses.GitBus;
import run.innkeeper.events.actions.builds.CheckGitBuild;
import run.innkeeper.events.actions.builds.MonitorBuild;
import run.innkeeper.events.actions.builds.MonitorGit;
import run.innkeeper.events.actions.builds.StartBuild;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.build.crd.BuildState;
import run.innkeeper.v1.build.crd.BuildStatus;
import run.innkeeper.v1.deployment.crd.Deployment;

import java.util.concurrent.TimeUnit;

@ControllerConfiguration()
public class BuildReconciler implements Reconciler<Build>, Cleaner<Build> {

    EventBus eventBus = EventBus.get();
    BuildBus buildBus = BuildBus.get();
    GitBus gitBus = GitBus.get();

    @Override
    public UpdateControl<Build> reconcile(Build build, Context<Build> context) throws Exception {
        Logging.debug("================== BUILD RECONCILE =========================");
        if (build.getStatus() == null) {
            build.setStatus(new BuildStatus());
            build.getStatus().setState(BuildState.WAITING);
        } else {
            switch (build.getStatus().getState()) {
                case WAITING -> eventBus.fire(new CheckGitBuild(build));
                case BUILDING -> eventBus.fire(new MonitorBuild(build));
                case GIT_CHECK -> eventBus.fire(new MonitorGit(build));
                case NEED_TO_BUILD -> eventBus.get().fire(new StartBuild(build));
            }
        }
        return UpdateControl.patchStatus(build).rescheduleAfter(3, TimeUnit.SECONDS);
    }
    // Return the changed resource, so it gets updated. See javadoc for details.

    public DeleteControl cleanup(Build build, Context<Build> context) {
        try {
            buildBus.delete(build.getSpec().getBuildSettings());
        } catch (Exception e) {e.printStackTrace();}
        try {
            gitBus.delete(build.getSpec().getBuildSettings());
        } catch (Exception e) {e.printStackTrace();}
        return DeleteControl.defaultDelete();
    }
}