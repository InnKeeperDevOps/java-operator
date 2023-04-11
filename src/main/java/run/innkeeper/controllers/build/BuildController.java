package run.innkeeper.controllers.build;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import run.innkeeper.buses.EventBus;
import run.innkeeper.buses.GitBus;
import run.innkeeper.events.actions.builds.CheckGitBuild;
import run.innkeeper.events.actions.builds.MonitorBuild;
import run.innkeeper.events.actions.builds.MonitorGit;
import run.innkeeper.events.actions.builds.StartBuild;
import run.innkeeper.events.builds.CreateBuild;
import run.innkeeper.events.builds.BuildFinished;
import run.innkeeper.events.structure.Trigger;
import run.innkeeper.services.K8sService;
import run.innkeeper.buses.BuildBus;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.build.crd.*;

public class BuildController {
    K8sService k8sService = K8sService.get();
    @Trigger(CreateBuild.class)
    public void createBuild(CreateBuild event){
        Build buildObj = new Build();
        buildObj.setMetaData(event.getBuild().getNamespace(), event.getBuild().getName());
        buildObj.setStatus(new BuildStatus());
        buildObj.getStatus().setState(BuildState.WAITING);
        buildObj.getStatus().setCompleted(new BuiltContainer[]{});
        BuildSpec buildSpec = new BuildSpec();
        buildSpec.setBuildSettings(event.getBuild());
        buildObj.setSpec(buildSpec);
        k8sService.createBuild(buildObj);
    }
    @Trigger(BuildFinished.class)
    public void buildFinished(BuildFinished event){
        if(event!=null) {
            Logging.info("WOOOT "+event.getBuild().getMetadata().getName());
        }
    }
    @Trigger(StartBuild.class)
    public void startBuild(StartBuild event){
        Job job = BuildBus.get().get(event.getBuild().getSpec().getBuildSettings());
        if(job==null){
            BuildBus.get().create(event.getBuild().getSpec().getBuildSettings());
        }
        EventBus.get().fire(new MonitorBuild(event.getBuild()));
    }
    @Trigger(MonitorBuild.class)
    public void monitorBuild(MonitorBuild event){
        Job job = BuildBus.get().get(event.getBuild().getSpec().getBuildSettings());
        if(job!=null){

        }
    }

    @Trigger(MonitorGit.class)
    public void monitorGit(MonitorGit event){
        Job job = GitBus.get().get(event.getBuild().getSpec().getBuildSettings());
        if(job!=null){
            Logging.info(job);
            Logging.info(k8sService.logs(job));
        }
    }
    @Trigger(CheckGitBuild.class)
    public void checkGitBuild(CheckGitBuild event){
        Job job = GitBus.get().get(event.getBuild().getSpec().getBuildSettings());
        if(job==null) {
            job = GitBus.get().create(event.getBuild().getSpec().getBuildSettings());
            event.getBuild().getStatus().setState(BuildState.GIT_CHECK);
            event.getBuild().getStatus().setJobName(job.getMetadata().getName());
        }else{
            event.getBuild().getStatus().setState(BuildState.GIT_CHECK);
            event.getBuild().getStatus().setJobName(job.getMetadata().getName());
        }
    }
}
