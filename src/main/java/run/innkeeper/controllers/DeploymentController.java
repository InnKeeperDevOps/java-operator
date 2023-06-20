package run.innkeeper.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.diff.JsonDiff;
import run.innkeeper.buses.DeploymentBus;
import run.innkeeper.events.actions.deployments.CheckDeployment;
import run.innkeeper.events.actions.deployments.CreateDeployment;
import run.innkeeper.events.actions.deployments.UpdateDeployment;
import run.innkeeper.events.structure.BuildWithContainer;
import run.innkeeper.events.structure.Trigger;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.deployment.crd.DeploymentState;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DeploymentController{
  K8sService k8sService = K8sService.get();
  DeploymentBus deploymentBus = DeploymentBus.get();

  private class BuildResult{
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
    List<String> missingContainers = deployment.getSpec().getDeploymentSettings().getContainers().stream().map(container -> container.getBuildName()).collect(Collectors.toList());
    List<BuildWithContainer> builds = deployment.getSpec().getDeploymentSettings().getContainers().stream().map(container -> {
      Build build = k8sService.getBuildByNamespaceAndName(deployment.getMetadata().getNamespace(), container.getBuildName());
      if (build != null &&
          build.getStatus() != null &&
          build.getStatus().getCompleted() != null &&
          build.getStatus().getCompleted().size() > 0
      ) {
        missingContainers.remove(build.getMetadata().getName());
      }
      return new BuildWithContainer(build, container);
    }).collect(Collectors.toList());
    return new BuildResult(missingContainers, builds);
  }

  @Trigger(CreateDeployment.class)
  public void createDeployment(CreateDeployment event) {
    BuildResult buildResult = getBuilds(event.getDeployment());
    if (buildResult.getMissingContainers().size() == 0) {
      io.fabric8.kubernetes.api.model.apps.Deployment deployment = deploymentBus.createDeployment(
          event.getDeployment().getSpec().getDeploymentSettings(),
          buildResult.builds.stream().reduce(
              new HashMap<>(),
              (list, b) -> {
                list.put(b.getContainer().getBuildName(), b.getBuild());
                return list;
              },
              (list, b) -> list
          )
      );
      if (deployment != null) {
        event.getDeployment().getStatus().setState(DeploymentState.DEPLOYED);
      }
    } else {
      Logging.error("Missing builds [" + buildResult.getMissingContainers().stream().collect(Collectors.joining(", ")) + "]");
    }
  }

  @Trigger(UpdateDeployment.class)
  public void updateDeployment(UpdateDeployment event) {
    BuildResult buildResult = getBuilds(event.getDeployment());
    if (buildResult.getMissingContainers().size() == 0) {
      io.fabric8.kubernetes.api.model.apps.Deployment deployment = deploymentBus.updateDeployment(event.getDeployment().getSpec().getDeploymentSettings(), buildResult.builds.stream().reduce(
          new HashMap<>(),
          (list, b) -> {
            list.put(b.getContainer().getBuildName(), b.getBuild());
            return list;
          },
          (list, b) -> list
      ));
      if (deployment != null) {
        event.getDeployment().getStatus().setState(DeploymentState.DEPLOYED);
      }
    } else {
      Logging.error("Missing builds [" + buildResult.getMissingContainers().stream().collect(Collectors.joining(", ")) + "]");
    }
  }

  @Trigger(CheckDeployment.class)
  public void checkDeployment(CheckDeployment event) throws JsonProcessingException {
    BuildResult buildResult = getBuilds(event.getDeployment());
    if (buildResult.getMissingContainers().size() == 0) {
      io.fabric8.kubernetes.api.model.apps.Deployment deployment = deploymentBus.buildDeployment(event.getDeployment().getSpec().getDeploymentSettings(), buildResult.builds.stream().reduce(
          new HashMap<>(),
          (list, b) -> {
            list.put(b.getContainer().getBuildName(), b.getBuild());
            return list;
          },
          (list, b) -> list
      ));

      io.fabric8.kubernetes.api.model.apps.Deployment liveDeployment = deploymentBus.get(event.getDeployment().getSpec().getDeploymentSettings());
      if (liveDeployment == null) {
        event.getDeployment().getStatus().setState(DeploymentState.NEED_TO_DEPLOY);
        return;
      }
      ObjectMapper om = new ObjectMapper();
      String currentDeployment = om.writeValueAsString(liveDeployment.getSpec().getTemplate().getSpec().getContainers());
      String newDeploymentData = om.writeValueAsString(deployment.getSpec());

      String newDeployment = om.writeValueAsString(deployment.getSpec().getTemplate().getSpec().getContainers());
      JsonPatch patch = JsonDiff.asJsonPatch(om.readTree(currentDeployment), om.readTree(newDeployment));
      String jsonPatch = om.writeValueAsString(patch);
      JsonReader reader = Json.createReader(new StringReader(jsonPatch));
      JsonArray patchOperations = reader.readArray();
      if (patchOperations.stream().filter(patchOp -> {
        JsonValue jn = patchOp.asJsonObject().get("path");
        return jn != null && jn.toString().endsWith("/image\"");
      }).collect(Collectors.toList()).size() > 0) {
        liveDeployment.setSpec(deployment.getSpec());
        deploymentBus.updateDeployment(liveDeployment);
      }

    } else {
      Logging.error("Missing builds [" + buildResult.getMissingContainers().stream().collect(Collectors.joining(", ")) + "]");
    }
  }
}
