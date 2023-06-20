package run.innkeeper.buses;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpec;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;

public class GitBus extends JobBus{
  private static GitBus bus = new GitBus();

  public GitBus() {
    super("git", "ghcr.io/innkeeperdevops/git-latest:13");
  }

  public static GitBus get() {
    return bus;
  }
}