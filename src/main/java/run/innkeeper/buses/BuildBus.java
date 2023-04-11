package run.innkeeper.buses;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpec;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.HashGenerator;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BuildBus extends JobBus {
    private static BuildBus bus = new BuildBus();

    public BuildBus() {
        super("build", "ghcr.io/innkeeperdevops/git-builder:28");
    }

    public static BuildBus get(){
        return bus;
    }

}
