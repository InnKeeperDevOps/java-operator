package run.innkeeper;

import run.innkeeper.buses.EventBus;
import run.innkeeper.events.server.ServerStarted;
import run.innkeeper.services.K8sService;

public class Main {
    static K8sService k8sService = K8sService.get();
    static EventBus eventBus = EventBus.get();
    public static void main(String[] args) {
        k8sService.createCRDsIfNotExists();
        eventBus.register();
        eventBus.fire(new ServerStarted());
    }
}