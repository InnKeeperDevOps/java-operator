package run.innkeeper.controllers;

import io.javaoperatorsdk.operator.Operator;
import org.springframework.boot.SpringApplication;
import run.innkeeper.api.ApiServer;
import run.innkeeper.buses.ExtensionBus;
import run.innkeeper.events.server.ServerStarted;
import run.innkeeper.events.structure.Trigger;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.build.BuildReconciler;
import run.innkeeper.v1.deployment.DeploymentReconciler;
import run.innkeeper.v1.guest.GuestReconciler;
import run.innkeeper.v1.service.ServiceReconciler;
import run.innkeeper.v1.simpleExtensions.SimpleExtensionReconciler;

/**
 * The type Server controller.
 */
public class ServerController {

  /**
   * The Extension bus.
   */
  ExtensionBus extensionBus = ExtensionBus.getExtensionBus();

  /**
   * Server started.
   */
  @Trigger(ServerStarted.class)
  public void serverStarted() {
    extensionBus.init();
    new Thread(() -> {
      Operator operator = new Operator();
      operator.register(new GuestReconciler());
      operator.register(new BuildReconciler());
      operator.register(new DeploymentReconciler());
      operator.register(new ServiceReconciler());
      operator.register(new SimpleExtensionReconciler());
      operator.start();
    }).start();
    new Thread(() -> SpringApplication.run(ApiServer.class, new String[0])).start();
  }

  /**
   * Start announce.
   */
  @Trigger(ServerStarted.class)
  public void startAnnounce() {
    Logging.info("Server started!");
  }
}
