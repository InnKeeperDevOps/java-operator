package run.innkeeper.buses;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpec;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.HashGenerator;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JobBus{

  K8sService k8sService = K8sService.get();

  public JobBus(String type, String image) {
    this.type = type;
    this.image = image;
  }

  private String type;
  private String image;

  public Job create(BuildSettings buildSettings) {
    Job job = newJob(buildSettings);
    job.setSpec(new JobSpec());
    job.getSpec().setTemplate(podTemplateSpec(buildSettings));
    this.k8sService.createJob(job);
    return job;
  }

  public void delete(BuildSettings buildSettings) {
    this.k8sService.deleteJob(newJob(buildSettings));
  }

  public Job get(Job job) {
    return this.k8sService.getJob(job);
  }

  public Job get(BuildSettings buildSettings) {
    return this.k8sService.getJob(newJob(buildSettings));
  }

  public Container getJobContainer(BuildSettings buildSettings) {
    Container container = new Container();
    container.setName(getName(buildSettings));
    container.setEnv(getEnVars(buildSettings));
    container.setImage(image);
    SecurityContext securityContext = new SecurityContext();
    securityContext.setAllowPrivilegeEscalation(true);
    securityContext.setPrivileged(true);
    container.setSecurityContext(securityContext);
    container.setVolumeMounts(getMounts(buildSettings));
    return container;
  }

  public PodTemplateSpec podTemplateSpec(BuildSettings buildSettings) {
    PodSpec podSpec = new PodSpec();
    podSpec.setVolumes(getVolumes(buildSettings));
    podSpec.setRestartPolicy("Never");
    podSpec.setContainers(Arrays.asList(getJobContainer(buildSettings)));
    return new PodTemplateSpec(getObjectMeta(buildSettings), podSpec);
  }

  public VolumeMount sshVolumeMount(BuildSettings buildSettings) {
    VolumeMount volumeMount = new VolumeMount();
    volumeMount.setName(buildSettings.getGit().getSecret());
    volumeMount.setMountPath("/ssh/");
    volumeMount.setReadOnly(true);
    return volumeMount;
  }

  private List<VolumeMount> getMounts(BuildSettings buildSettings) {
    List<VolumeMount> volumeMounts = new LinkedList<>();
    volumeMounts.add(sshVolumeMount(buildSettings));
    return volumeMounts;
  }

  private Volume getSecretVolume(BuildSettings buildSettings) {
    Volume volume = new Volume();
    volume.setName(buildSettings.getGit().getSecret());
    volume.setSecret(getVolumeSecretSource(buildSettings));
    return volume;
  }

  private SecretVolumeSource getVolumeSecretSource(BuildSettings buildSettings) {
    SecretVolumeSource secretVolumeSource = new SecretVolumeSource();
    secretVolumeSource.setSecretName(buildSettings.getGit().getSecret());
    secretVolumeSource.setItems(Arrays.asList(new KeyToPath("ssh-key", 0555, "key")));
    return secretVolumeSource;
  }

  private List<Volume> getVolumes(BuildSettings buildSettings) {
    List<Volume> volumes = new LinkedList<>();
    volumes.add(getSecretVolume(buildSettings));
    return volumes;
  }

  private List<EnvVar> getEnVars(BuildSettings buildSettings) {
    List<EnvVar> envars = new LinkedList<>();
    envars.add(new EnvVar("GIT_REPO", buildSettings.getGit().getUri(), null));
    envars.add(new EnvVar("REGISTRY_HOST", buildSettings.getPublish().getRegistry(), null));
    envars.add(new EnvVar("DOCKER_TAG", buildSettings.getPublish().getTag(), null));
    envars.add(new EnvVar("REGISTRY_USERNAME", null, new EnvVarSource(){{
      setSecretKeyRef(new SecretKeySelector("username", buildSettings.getPublish().getSecret(), false));
    }}));
    envars.add(new EnvVar("REGISTRY_PASSWORD", null, new EnvVarSource(){{
      setSecretKeyRef(new SecretKeySelector("password", buildSettings.getPublish().getSecret(), false));
    }}));
    if (buildSettings.getDocker() != null && buildSettings.getDocker().getDockerfile() != null) {
      envars.add(new EnvVar("DOCKERFILE", buildSettings.getDocker().getDockerfile(), null));
    } else {
      envars.add(new EnvVar("DOCKERFILE", "Dockerfile", null));
    }
    if (buildSettings.getDocker() != null && buildSettings.getDocker().getWorkdir() != null) {
      envars.add(new EnvVar("WORKDIR", buildSettings.getDocker().getWorkdir(), null));
    } else {
      envars.add(new EnvVar("WORKDIR", ".", null));
    }
    if (buildSettings.getGit().getBranch() != null) {
      envars.add(new EnvVar("GIT_BRANCH", buildSettings.getGit().getBranch(), null));
    }
    if (buildSettings.getGit().getCommit() != null) {
      envars.add(new EnvVar("GIT_COMMIT", buildSettings.getGit().getCommit(), null));
    }
    return envars;
  }

  public Job newJob(BuildSettings buildSettings) {
    Job job = new Job();
    job.setMetadata(getObjectMeta(buildSettings));
    return job;
  }

  private ObjectMeta getObjectMeta(BuildSettings buildSettings) {
    ObjectMeta objectMeta = new ObjectMeta();
    objectMeta.setName(getName(buildSettings));
    objectMeta.setNamespace(buildSettings.getNamespace());
    return objectMeta;
  }

  private String getName(BuildSettings buildSettings) {
    return type + "-" + HashGenerator.getJobName(buildSettings.getNamespace(), buildSettings.getName());
  }
}
