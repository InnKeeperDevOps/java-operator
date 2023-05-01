package run.innkeeper.controllers;

import run.innkeeper.buses.DeploymentBus;
import run.innkeeper.events.actions.deployments.CreateDeployment;
import run.innkeeper.events.actions.deployments.UpdateDeployment;
import run.innkeeper.events.structure.BuildWithContainer;
import run.innkeeper.events.structure.Trigger;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.deployment.crd.DeploymentState;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DeploymentController {
    K8sService k8sService = K8sService.get();
    DeploymentBus deploymentBus = DeploymentBus.get();

    private class BuildResult {
        List<String> missingContainers;
        List<BuildWithContainer> builds;
        public BuildResult(List<String> missingContainers, List<BuildWithContainer> builds) {
            this.missingContainers = missingContainers;
            this.builds = builds;
        }
        public List<String> getMissingContainers() {
            return missingContainers;
        }
        public List<BuildWithContainer> getBuilds() {
            return builds;
        }
    }
    private BuildResult getBuilds(Deployment deployment) {
        List<String> missingContainers = deployment.getSpec().getDeploymentSettings().getContainers().stream().map(container->container.getBuildName()).collect(Collectors.toList());
        List<BuildWithContainer> builds = deployment.getSpec().getDeploymentSettings().getContainers().stream().map(container -> {
            Build build = k8sService.getBuildByNamespaceAndName(deployment.getMetadata().getNamespace(), container.getBuildName());
            if (build!=null &&
                build.getStatus()!=null &&
                build.getStatus().getCompleted()!=null &&
                build.getStatus().getCompleted().size()>0
            ) {
                missingContainers.remove(build.getMetadata().getName());
            }
            return new BuildWithContainer(build,container);
        }).collect(Collectors.toList());
        return new BuildResult(missingContainers, builds);
    }

    @Trigger(CreateDeployment.class)
    public void createDeployment(CreateDeployment event){
        BuildResult buildResult = getBuilds(event.getDeployment());
        if(buildResult.getMissingContainers().size()==0){
            io.fabric8.kubernetes.api.model.apps.Deployment deployment = deploymentBus.createDeployment(
                event.getDeployment().getSpec().getDeploymentSettings(),
                buildResult.builds.stream().reduce(
                    new HashMap<>(),
                    (list,b)->{
                        list.put(b.getContainer().getBuildName(), b.getBuild());
                        return list;
                    },
                    (list,b)->list
                )
            );
            if(deployment!=null){
                event.getDeployment().getStatus().setState(DeploymentState.DEPLOYED);
            }
        }else{
            Logging.error("Missing builds ["+buildResult.getMissingContainers().stream().collect(Collectors.joining(", "))+"]");
        }
    }

    @Trigger(UpdateDeployment.class)
    public void updateDeployment(UpdateDeployment event){
        BuildResult buildResult = getBuilds(event.getDeployment());
        if(buildResult.getMissingContainers().size()==0){
            io.fabric8.kubernetes.api.model.apps.Deployment deployment = deploymentBus.updateDeployment(event.getDeployment().getSpec().getDeploymentSettings(), buildResult.builds.stream().reduce(
                new HashMap<>(),
                (list,b)->{
                    list.put(b.getContainer().getBuildName(), b.getBuild());
                    return list;
                },
                (list,b)->list
            ));
            if(deployment!=null){
                event.getDeployment().getStatus().setState(DeploymentState.DEPLOYED);
            }
        }else{
            Logging.error("Missing builds ["+buildResult.getMissingContainers().stream().collect(Collectors.joining(", "))+"]");
        }
    }
}
