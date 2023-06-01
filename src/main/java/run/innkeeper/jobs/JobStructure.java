package run.innkeeper.jobs;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.SecretKeySelector;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpec;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JobStructure{
  private String name;
  private String image;

  private Map<String, String> enVars;

  K8sService k8sService = K8sService.get();

  public JobStructure(String name, String image, Map<String, String> enVars) {
    this.name = name;
    this.image = image;
    this.enVars = enVars;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public void create(){
    this.k8sService.createJob(newJob());
  }

  public void delete(){
    this.k8sService.deleteJob(newJob());
  }

  public Job get(){
    return this.k8sService.getJob(newJob());
  }

  public String logs(){
    return k8sService.logs(newJob());
  }
  public InputStream logStream(){
    return k8sService.logStream(newJob());
  }

  public LogWatch logWatch(){
    return k8sService.logWatch(newJob());
  }

  public Job newJob(){
    Job job = new JobBuilder()
      .withNewMetadata()
        .withName(name)
        .withNamespace("innkeeper")
      .endMetadata()
      .withNewSpec()
        .withNewTemplate()
          .withNewSpec()
            .withRestartPolicy("Never")
            .addNewContainer()
              .addAllToEnv(this.getEnVarsList())
              .withImage(this.image)
              .withName(name)
            .endContainer()
          .endSpec()
        .endTemplate()
      .endSpec()
      .build();
    return job;
  }
  private List<EnvVar> getEnVarsList() {
    List<EnvVar> envars = new LinkedList<>();
    this.enVars.entrySet().forEach(enVar->{
      envars.add(new EnvVar(enVar.getKey(), enVar.getValue(), null));
    });
    return envars;
  }

  public Map<String, String> getEnVars() {
    return enVars;
  }

  public void setEnVars(Map<String, String> enVars) {
    this.enVars = enVars;
  }
}
