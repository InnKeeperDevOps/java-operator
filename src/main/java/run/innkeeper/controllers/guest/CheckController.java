package run.innkeeper.controllers.guest;

import run.innkeeper.buses.EventBus;
import run.innkeeper.events.actions.builds.UpdateBuild;
import run.innkeeper.events.builds.CreateBuild;
import run.innkeeper.events.actions.guests.CheckGuestBuildChanges;
import run.innkeeper.events.structure.Trigger;
import run.innkeeper.utilities.HashGenerator;
import run.innkeeper.utilities.Logging;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.Comparison;
import run.innkeeper.v1.build.crd.Build;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CheckController {
    K8sService k8sService = K8sService.get();
    @Trigger(CheckGuestBuildChanges.class)
    public void checkIfUpdated(CheckGuestBuildChanges event) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Logging.debug("Checking for updates to build "+event.getBuild().getName());
        String name = event.getBuild().getName();
        String namespace = event.getBuild().getNamespace();
        String buildCRName = HashGenerator.getBuildName(namespace, name);
        Build buildObj = new Build();
        buildObj.setMetaData(namespace, name);
        buildObj = k8sService.getBuildByNamespaceAndName(buildObj);
        if(buildObj!=null) {
            List<String> changedFields = new LinkedList<>();
            changedFields.addAll(Comparison.compare(
                event.getBuild().getPublish(),
                buildObj.getSpec().getBuildSettings().getPublish(),
                "publish",
                "registry",
                "secret",
                "tag"
            ));
            changedFields.addAll(Comparison.compare(
                event.getBuild().getDocker(),
                buildObj.getSpec().getBuildSettings().getDocker(),
                "docker",
                "dockerfile",
                "workdir"
            ));
            changedFields.addAll(Comparison.compare(
                event.getBuild().getGit(),
                buildObj.getSpec().getBuildSettings().getGit(),
                "git",
                "branch",
                "uri",
                "secret"
            ));
            if(changedFields.size()>0){
                Logging.info("Changes detected: "+changedFields.stream().collect(Collectors.joining(", ")));
                EventBus.get().fire(new UpdateBuild(buildObj, event.getBuild(), changedFields));
            }
        }else{
            Logging.info("New build detected, creating Build CRD object");
            EventBus.get().fire(new CreateBuild(event.getBuild()));
        }

    }



}
