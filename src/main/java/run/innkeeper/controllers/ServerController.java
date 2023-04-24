package run.innkeeper.controllers;

import io.javaoperatorsdk.operator.Operator;
import run.innkeeper.events.server.ServerStarted;
import run.innkeeper.events.structure.Trigger;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.build.BuildReconciler;
import run.innkeeper.v1.deployment.DeploymentReconciler;
import run.innkeeper.v1.guest.GuestReconciler;

public class ServerController {

    @Trigger(ServerStarted.class)
    public void serverStarted(){
        new Thread(()->{
            Operator operator = new Operator();
            operator.register(new GuestReconciler());
            operator.register(new BuildReconciler());
            operator.register(new DeploymentReconciler());
            operator.start();
        }).start();
    }

    @Trigger(ServerStarted.class)
    public void startAnnounce(){
        Logging.info("Server started!");
    }
}
