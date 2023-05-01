package run.innkeeper.v1.network;

import io.javaoperatorsdk.operator.api.reconciler.*;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.network.crd.Traffic;

@ControllerConfiguration()
public class TrafficReconciler implements Reconciler<Traffic>, Cleaner<Traffic> {
    K8sService k8sService = K8sService.get();


    @Override
    public DeleteControl cleanup(Traffic network, Context<Traffic> context) {

        return null;
    }

    @Override
    public UpdateControl<Traffic> reconcile(Traffic network, Context<Traffic> context) throws Exception {

        return null;
    }
}
