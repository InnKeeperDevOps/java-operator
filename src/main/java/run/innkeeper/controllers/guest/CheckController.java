package run.innkeeper.controllers.guest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.diff.JsonDiff;
import run.innkeeper.buses.EventBus;
import run.innkeeper.events.actions.builds.UpdateBuild;
import run.innkeeper.events.actions.guests.CheckGuestBuildChanges;
import run.innkeeper.events.actions.guests.CheckGuestDeploymentChanges;
import run.innkeeper.events.structure.Trigger;
import run.innkeeper.utilities.HashGenerator;
import run.innkeeper.utilities.Logging;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.Comparison;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.build.crd.BuildSpec;
import run.innkeeper.v1.build.crd.BuildState;
import run.innkeeper.v1.build.crd.BuildStatus;
import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.deployment.crd.DeploymentSpec;
import run.innkeeper.v1.deployment.crd.DeploymentState;
import run.innkeeper.v1.deployment.crd.DeploymentStatus;
import run.innkeeper.v1.guest.crd.objects.deployment.Container;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.beans.IntrospectionException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CheckController {
    K8sService k8sService = K8sService.get();
    static ObjectMapper om = new ObjectMapper();
    @Trigger(CheckGuestDeploymentChanges.class)
    public void checkIfDeploymentUpdated(CheckGuestDeploymentChanges event) throws JsonProcessingException {
        Logging.debug("Checking for updates to deployment "+event.getDeploymentSetting().getName());
        String name = event.getDeploymentSetting().getName();
        String namespace = event.getGuest().getMetadata().getNamespace();
        Deployment deploymentObj = new Deployment();
        deploymentObj.setMetaData(namespace, name);
        deploymentObj = k8sService.getDeploymentByNamespaceAndName(deploymentObj);
        if(deploymentObj!=null) {
            ObjectMapper om = new ObjectMapper();
            String deploymentNew = om.writeValueAsString(event.getDeploymentSetting());
            String deploymentOld = om.writeValueAsString(deploymentObj.getSpec().getDeploymentSettings());
            JsonPatch patch = JsonDiff.asJsonPatch(om.readTree(deploymentOld), om.readTree(deploymentNew));
            String jsonPatch = om.writeValueAsString(patch);
            JsonReader reader = Json.createReader(new StringReader(jsonPatch));
            JsonArray patchOperations = reader.readArray();
            if(patchOperations.size()>0){
                deploymentObj = new Deployment();
                deploymentObj.setMetaData(namespace, name);
                deploymentObj = k8sService.getDeploymentClient().resource(deploymentObj).get();
                deploymentObj.getSpec().setDeploymentSettings(event.getDeploymentSetting());
                deploymentObj = k8sService.getDeploymentClient().resource(deploymentObj).patch();
                deploymentObj.getStatus().setState(DeploymentState.REDEPLOY);
                k8sService.getDeploymentClient().resource(deploymentObj).patchStatus();
                event.getGuest().getStatus().getDeploymentChangeHistory().add(jsonPatch);
            }
        }else{
            Logging.info("New build detected, creating Build CRD object");
            deploymentObj = new Deployment();
            deploymentObj.setMetaData(namespace, name);
            deploymentObj.setStatus(new DeploymentStatus());
            deploymentObj.getStatus().setState(DeploymentState.NEED_TO_DEPLOY);
            DeploymentSpec buildSpec = new DeploymentSpec();
            buildSpec.setDeploymentSettings(event.getDeploymentSetting());
            deploymentObj.setSpec(buildSpec);
            k8sService.createDeployment(deploymentObj);
        }
    }
    @Trigger(CheckGuestBuildChanges.class)
    public void checkIfBuildUpdated(CheckGuestBuildChanges event) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Logging.debug("Checking for updates to build "+event.getBuild().getName());
        String name = event.getBuild().getName();
        String namespace = event.getGuest().getMetadata().getNamespace();
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
            buildObj = new Build();
            buildObj.setMetaData(namespace, name);
            buildObj.setStatus(new BuildStatus());
            buildObj.getStatus().setState(BuildState.WAITING);
            buildObj.getStatus().setCompleted(new ArrayList<>());
            BuildSpec buildSpec = new BuildSpec();
            buildSpec.setBuildSettings(event.getBuild());
            buildObj.setSpec(buildSpec);
            k8sService.createBuild(buildObj);
        }

    }



}
