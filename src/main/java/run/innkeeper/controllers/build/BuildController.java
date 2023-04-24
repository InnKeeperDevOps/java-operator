package run.innkeeper.controllers.build;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import run.innkeeper.buses.GitBus;
import run.innkeeper.events.actions.builds.CheckGitBuild;
import run.innkeeper.events.actions.builds.MonitorBuild;
import run.innkeeper.events.actions.builds.MonitorGit;
import run.innkeeper.events.actions.builds.StartBuild;
import run.innkeeper.events.builds.BuildFinished;
import run.innkeeper.events.structure.Trigger;
import run.innkeeper.services.K8sService;
import run.innkeeper.buses.BuildBus;
import run.innkeeper.utilities.BuildMonitor;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.build.crd.*;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.build.Docker;
import run.innkeeper.v1.guest.crd.objects.build.GitSource;
import run.innkeeper.v1.guest.crd.objects.build.Publish;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BuildController {
    K8sService k8sService = K8sService.get();

    @Trigger(BuildFinished.class)
    public void buildFinished(BuildFinished event) {
        if (event != null) {
            Logging.info("WOOOT " + event.getBuild().getMetadata().getName());
        }
    }

    @Trigger(StartBuild.class)
    public void startBuild(StartBuild event) {
        Job job = BuildBus.get().get(event.getBuild().getSpec().getBuildSettings());
        if (job == null) {
            BuildBus.get().create(event.getBuild().getSpec().getBuildSettings());
            event.getBuild().getStatus().setState(BuildState.BUILDING);
        }
    }

    @Trigger(MonitorBuild.class)
    public void monitorBuild(MonitorBuild event) {
        Job job = BuildBus.get().get(event.getBuild().getSpec().getBuildSettings());
        if (job != null) {
            if (job.getStatus() != null && job.getStatus().getSucceeded()!=null && job.getStatus().getSucceeded() == 1) {

                String logs = k8sService.logs(job);
                BuildMonitor.BuildLogParts buildMonitor = BuildMonitor.get(logs);
                BuildSettings buildSettings = event.getBuild().getSpec().getBuildSettings();

                BuiltContainer builtContainer = new BuiltContainer();

                GitSource gitSource = new GitSource(buildSettings.getGit());
                gitSource.setCommit(buildMonitor.hash);
                gitSource.setBranch(buildMonitor.branch);
                builtContainer.setGitSource(gitSource);

                if( buildSettings.getDocker()!=null )
                    builtContainer.setDocker(new Docker(buildSettings.getDocker()));

                builtContainer.setPublish(new Publish(buildSettings.getPublish()));

                builtContainer.setNamespace(event.getBuild().getSpec().getBuildSettings().getNamespace());
                builtContainer.setJobName(job.getMetadata().getName());

                if(event.getBuild().getStatus().getCompleted() == null){
                    event.getBuild().getStatus().setCompleted(new ArrayList<>());
                }
                event.getBuild().getStatus().getCompleted().add(builtContainer);
                event.getBuild().getStatus().setState(BuildState.WAITING);
                k8sService.deleteJob(job);
            } else if (job.getStatus() != null && job.getStatus().getFailed()!=null && job.getStatus().getFailed() == 1) {
                event.getBuild().getStatus().setState(BuildState.BUILD_FAILED);
            } else {
                Logging.info("waiting for build to complete");
            }
        }
    }

    @Trigger(MonitorGit.class)
    public void monitorGit(MonitorGit event) {
        Job job = GitBus.get().get(event.getBuild().getSpec().getBuildSettings());
        if (job != null) {
            if (job.getStatus()!=null && job.getStatus().getSucceeded()!=null && job.getStatus().getSucceeded() == 1) {
                String commit = null;
                Pattern pattern = Pattern.compile("[a-f0-9]{40}");
                Matcher matcher = pattern.matcher(k8sService.logs(job));
                while (matcher.find()) {
                    commit = matcher.group();
                }
                String finalCommit = commit;
                List<BuiltContainer> builtContainers = new ArrayList<>();
                if (event.getBuild().getStatus().getCompleted() != null) {
                    builtContainers = event.getBuild().getStatus().getCompleted()
                        .stream().filter(builtContainer -> builtContainer.getGitSource().getCommit().equals(finalCommit))
                        .collect(Collectors.toList());
                }
                if (builtContainers.size() == 0) {
                    event.getBuild().getStatus().setState(BuildState.NEED_TO_BUILD);
                    k8sService.deleteJob(job);
                } else {
                    event.getBuild().getStatus().setState(BuildState.WAITING);
                    k8sService.deleteJob(job);
                }
            } else {
                Logging.info("waiting for git check to complete");
            }
        }
    }

    @Trigger(CheckGitBuild.class)
    public void checkGitBuild(CheckGitBuild event) {
        Job job = GitBus.get().get(event.getBuild().getSpec().getBuildSettings());
        if (job == null) {
            job = GitBus.get().create(event.getBuild().getSpec().getBuildSettings());
        }
        event.getBuild().getStatus().setState(BuildState.GIT_CHECK);
        event.getBuild().getStatus().setJobName(job.getMetadata().getName());
    }
}
