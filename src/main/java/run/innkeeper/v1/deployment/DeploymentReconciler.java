package run.innkeeper.v1.deployment;

import io.javaoperatorsdk.operator.api.reconciler.*;
import run.innkeeper.buses.EventBus;
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
public class DeploymentReconciler implements Reconciler<Deployment> {
    K8sService k8sService = K8sService.get();
    @Override
    public UpdateControl<Deployment> reconcile(Deployment deployment, Context<Deployment> context) throws Exception {
        Logging.debug("================== DEPLOYMENT RECONCILE =========================");
        DeploymentSettings deploymentSettings = deployment.getSpec().getDeploymentSettings();
        EventBus eventBus = EventBus.get();
        if(deployment.getStatus() == null){
            deployment.setStatus(new DeploymentStatus());
            deployment.getStatus().setState(DeploymentState.NEED_TO_DEPLOY);
        }else {
            switch (deployment.getStatus().getState()) {
                case REDEPLOY -> eventBus.get().fire(new UpdateDeployment(deployment));
                case NEED_TO_DEPLOY -> eventBus.get().fire(new CreateDeployment(deployment));
            }
        }
        return UpdateControl.patchStatus(deployment).rescheduleAfter(5, TimeUnit.SECONDS);
    }
}
