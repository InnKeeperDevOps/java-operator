package run.innkeeper.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.diff.JsonDiff;
import run.innkeeper.buses.EventBus;
import run.innkeeper.buses.ServiceBus;
import run.innkeeper.events.actions.builds.UpdateBuild;
import run.innkeeper.events.actions.guests.*;
import run.innkeeper.events.structure.Trigger;
import run.innkeeper.utilities.Logging;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.build.crd.BuildSpec;
import run.innkeeper.v1.build.crd.BuildState;
import run.innkeeper.v1.build.crd.BuildStatus;
import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.deployment.crd.DeploymentSpec;
import run.innkeeper.v1.deployment.crd.DeploymentState;
import run.innkeeper.v1.deployment.crd.DeploymentStatus;
import run.innkeeper.v1.service.crd.Service;
import run.innkeeper.v1.service.crd.ServiceSpec;
import run.innkeeper.v1.service.crd.ServiceState;
import run.innkeeper.v1.service.crd.ServiceStatus;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.beans.IntrospectionException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class GuestController {
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
    public void checkIfBuildUpdated(CheckGuestBuildChanges event) throws IntrospectionException, InvocationTargetException, IllegalAccessException, JsonProcessingException {
        Logging.debug("Checking for updates to build "+event.getBuild().getName());
        String name = event.getBuild().getName();
        String namespace = event.getGuest().getMetadata().getNamespace();
        Build buildObj = new Build();
        buildObj.setMetaData(namespace, name);
        buildObj = k8sService.getBuildByNamespaceAndName(buildObj);
        if(buildObj!=null) {
            ObjectMapper om = new ObjectMapper();
            String buildNew = om.writeValueAsString(event.getBuild());
            String buildOld = om.writeValueAsString(buildObj.getSpec().getBuildSettings());
            JsonPatch patch = JsonDiff.asJsonPatch(om.readTree(buildOld), om.readTree(buildNew));
            String jsonPatch = om.writeValueAsString(patch);
            JsonReader reader = Json.createReader(new StringReader(jsonPatch));
            JsonArray patchOperations = reader.readArray();
            if(patchOperations.size()>0){
                buildObj.setMetaData(namespace, name);
                buildObj = k8sService.getBuildClient().resource(buildObj).get();
                buildObj.getSpec().setBuildSettings(event.getBuild());
                buildObj = k8sService.getBuildClient().resource(buildObj).patch();
                buildObj.getStatus().setState(BuildState.NEED_TO_BUILD);
                k8sService.getBuildClient().resource(buildObj).patchStatus();
                EventBus.get().fire(new UpdateBuild(buildObj, event.getBuild()));
                event.getGuest().getStatus().getDeploymentChangeHistory().add(jsonPatch);
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

    @Trigger(CheckGuestServiceChanges.class)
    public void checkGuestServiceChanges(CheckGuestServiceChanges event) throws JsonProcessingException {
        Logging.debug("Checking for updates to service "+event.getServiceSettings().getName());
        String name = event.getServiceSettings().getName();
        String namespace = event.getGuest().getMetadata().getNamespace();
        Service service = new Service();
        service.setMetaData(namespace, name);
        service = k8sService.getServiceClient().resource(service).get();
        if(service!=null) {
            ObjectMapper om = new ObjectMapper();
            String buildNew = om.writeValueAsString(event.getServiceSettings());
            String buildOld = om.writeValueAsString(service.getSpec().getServiceSettings());
            JsonPatch patch = JsonDiff.asJsonPatch(om.readTree(buildOld), om.readTree(buildNew));
            String jsonPatch = om.writeValueAsString(patch);
            JsonReader reader = Json.createReader(new StringReader(jsonPatch));
            JsonArray patchOperations = reader.readArray();
            if(patchOperations.size()>0){
                service.setMetaData(namespace, name);
                service = k8sService.getServiceClient().resource(service).get();
                service.getSpec().setServiceSettings(event.getServiceSettings());
                service = k8sService.getServiceClient().resource(service).patch();
                service.getStatus().setServiceState(ServiceState.NEED_TO_CREATE);
                k8sService.getServiceClient().resource(service).patchStatus();
                event.getGuest().getStatus().getServicesChangeHistory().add(jsonPatch);
            }
        }else{
            service = new Service();
            service.setMetaData(namespace, name);
            service.setStatus(new ServiceStatus());
            service.getStatus().setServiceState(ServiceState.NEED_TO_CREATE);
            ServiceSpec serviceSpec = new ServiceSpec();
            serviceSpec.setServiceSettings(event.getServiceSettings());
            service.setSpec(serviceSpec);
            k8sService.getServiceClient().resource(service).create();
        }
    }

    @Trigger(DeleteGuestBuild.class)
    public void deleteGuestBuild(DeleteGuestBuild event) {
        Build build = new Build();
        build.setMetaData(event.getGuest().getMetadata().getNamespace(), event.getBuildSettings().getName());
        k8sService.getBuildClient().resource(build).delete();
    }

    @Trigger(DeleteGuestDeployment.class)
    public void deleteGuestDeployment(DeleteGuestDeployment event) {
        Deployment deployment = new Deployment();
        deployment.setMetaData(event.getGuest().getMetadata().getNamespace(), event.getDeploymentSettings().getName());
        k8sService.getDeploymentClient().resource(deployment).delete();
    }

}
