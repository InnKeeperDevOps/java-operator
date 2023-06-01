package run.innkeeper.v1.deployment;

import io.javaoperatorsdk.operator.api.reconciler.*;
import run.innkeeper.buses.DeploymentBus;
import run.innkeeper.buses.EventBus;
import run.innkeeper.events.actions.deployments.CheckDeployment;
import run.innkeeper.events.actions.deployments.CreateDeployment;
import run.innkeeper.events.actions.deployments.UpdateDeployment;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.deployment.crd.DeploymentState;
import run.innkeeper.v1.deployment.crd.DeploymentStatus;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;

import java.util.concurrent.TimeUnit;

@ControllerConfiguration()
public class DeploymentReconciler implements Reconciler<Deployment>, Cleaner<Deployment>{
  K8sService k8sService = K8sService.get();
  DeploymentBus deploymentBus = DeploymentBus.get();

  @Override
  public UpdateControl<Deployment> reconcile(Deployment deployment, Context<Deployment> context) throws Exception {
    Logging.debug("================== DEPLOYMENT RECONCILE =========================");
    DeploymentSettings deploymentSettings = deployment.getSpec().getDeploymentSettings();
    EventBus eventBus = EventBus.get();
    if (deployment.getStatus() == null) {
      deployment.setStatus(new DeploymentStatus());
      deployment.getStatus().setState(DeploymentState.NEED_TO_DEPLOY);
    } else {
      switch (deployment.getStatus().getState()) {
        case REDEPLOY -> eventBus.get().fire(new UpdateDeployment(deployment));
        case NEED_TO_DEPLOY -> eventBus.get().fire(new CreateDeployment(deployment));
        case DEPLOYED -> eventBus.get().fire(new CheckDeployment(deployment));
      }
    }
    return UpdateControl.updateStatus(deployment).rescheduleAfter(3, TimeUnit.SECONDS);
  }

  @Override
  public DeleteControl cleanup(Deployment resource, Context<Deployment> context) {
    Logging.info("Deleting Deployment");
    try {
      deploymentBus.deleteDeployment(resource.getSpec().getDeploymentSettings());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return DeleteControl.defaultDelete();
  }
}
