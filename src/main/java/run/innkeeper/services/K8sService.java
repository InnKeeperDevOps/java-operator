package run.innkeeper.services;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.account.crd.Account;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.guest.crd.Guest;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.apiextensions.v1.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import run.innkeeper.v1.service.crd.Service;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;

import java.io.InputStream;
import java.util.*;

public class K8sService{
  public static K8sService singleton = new K8sService();
  KubernetesClient client;

  public K8sService() {
    client = new KubernetesClientBuilder().build();
  }

  public static K8sService get() {
    return singleton;
  }

  public KubernetesClient getClient() {
    return client;
  }

  public List<Namespace> getAllNamespaces() {
    return this.client.namespaces().list().getItems();
  }

  public List<CustomResourceDefinition> getAllCRDs() {
    return this.client.apiextensions().v1().customResourceDefinitions().list().getItems();
  }

  static class CRDObject{
    public Class clazz;
    public String file;

    public CRDObject(Class clazz, String file) {
      this.clazz = clazz;
      this.file = file;
    }
  }

  static Map<String, CRDObject> crds = new HashMap<>(){{
    put("guests.cicd.innkeeper.run", new CRDObject(Guest.class, "META-INF/fabric8/guests.cicd.innkeeper.run-v1.yml"));
    put("builds.cicd.innkeeper.run", new CRDObject(Build.class, "META-INF/fabric8/builds.cicd.innkeeper.run-v1.yml"));
    put("deployments.cicd.innkeeper.run", new CRDObject(Deployment.class, "META-INF/fabric8/deployments.cicd.innkeeper.run-v1.yml"));
    put("services.cicd.innkeeper.run", new CRDObject(Service.class, "META-INF/fabric8/services.cicd.innkeeper.run-v1.yml"));
    put("simpleextensions.cicd.innkeeper.run", new CRDObject(SimpleExtension.class, "META-INF/fabric8/simpleextensions.cicd.innkeeper.run-v1.yml"));
    put("accounts.cicd.innkeeper.run", new CRDObject(Account.class, "META-INF/fabric8/accounts.cicd.innkeeper.run-v1.yml"));
  }};

  public CustomResourceDefinition loadCRDFromFile(String crdFileDefYaml) {
    return this.client
        .apiextensions()
        .v1()
        .customResourceDefinitions()
        .load(
            K8sService.class
                .getClassLoader()
                .getResourceAsStream(crdFileDefYaml)
        )
        .item();
  }

  public void createCRDsIfNotExists() {
    Map<String, CRDObject> missing = new HashMap<>(crds);
    Set<String> keys = missing.keySet();
    this.getAllCRDs().stream().forEach(crd -> {
      //System.out.println(crd.getMetadata().getName());
      if (keys.contains(crd.getMetadata().getName())) {
        missing.remove(crd.getMetadata().getName());
      }
    });
    missing.forEach((crdName, clazz) -> {
      Logging.info("Applying " + crdName + " to the k8s cluster.");
      this.client
          .apiextensions()
          .v1()
          .customResourceDefinitions()
          .resource(loadCRDFromFile(clazz.file))
          .create();
    });
  }

  public String logs(Job job) {
    Job jobExists = client.resource(job).get();
    if (jobExists != null) {
      List<Pod> pods = client.pods().inNamespace(jobExists.getMetadata().getNamespace()).withLabel("job-name", jobExists.getMetadata().getName()).list().getItems();
      if (pods.size() == 1) {
        Pod pod = pods.get(0);
        String containerName = pod.getSpec().getContainers().get(0).getName();
        String podName = pod.getMetadata().getName();
        String namespace = pod.getMetadata().getNamespace();
        return client.pods().inNamespace(namespace).withName(podName).inContainer(containerName).getLog();
      }
    }
    return null;
  }

  public InputStream logStream(Job job) {
    Job jobExists = client.resource(job).get();
    if (jobExists != null) {
      List<Pod> pods = client.pods().inNamespace(jobExists.getMetadata().getNamespace()).withLabel("job-name", jobExists.getMetadata().getName()).list().getItems();
      if (pods.size() == 1) {
        Pod pod = pods.get(0);
        String containerName = pod.getSpec().getContainers().get(0).getName();
        String podName = pod.getMetadata().getName();
        String namespace = pod.getMetadata().getNamespace();
        return client.pods().inNamespace(namespace).withName(podName).inContainer(containerName).getLogInputStream();
      }
    }
    return null;
  }
  public LogWatch logWatch(Job job) {
    Job jobExists = client.resource(job).get();
    if (jobExists != null) {
      List<Pod> pods = client.pods().inNamespace(jobExists.getMetadata().getNamespace()).withLabel("job-name", jobExists.getMetadata().getName()).list().getItems();
      if (pods.size() == 1) {
        Pod pod = pods.get(0);
        String containerName = pod.getSpec().getContainers().get(0).getName();
        String podName = pod.getMetadata().getName();
        String namespace = pod.getMetadata().getNamespace();
        return client.pods().inNamespace(namespace).withName(podName).inContainer(containerName).watchLog();
      }
    }
    return null;
  }

  public void createJob(Job job) {
    this.client.resource(job).create();
  }

  public void deleteJob(Job job) {
    this.client.resource(job).delete();
  }

  public Job getJob(Job job) {
    return this.client.resource(job).get();
  }

  public MixedOperation<Guest, KubernetesResourceList<Guest>, Resource<Guest>> getGuestClient() {
    return client.resources(Guest.class);
  }

  public MixedOperation<SimpleExtension, KubernetesResourceList<SimpleExtension>, Resource<SimpleExtension>> getSimpleExtensionClient() {
    return client.resources(SimpleExtension.class);
  }

  public SimpleExtension getSimpleExtension(SimpleExtension extension) {
    return getSimpleExtensionClient().resource(extension).get();
  }

  public SimpleExtension createSimpleExtension(SimpleExtension extension) {
    return getSimpleExtensionClient().resource(extension).create();
  }

  public SimpleExtension updateSimpleExtension(SimpleExtension extension) {
    return getSimpleExtensionClient().resource(extension).patch();
  }

  public SimpleExtension updateSimpleExtensionStatus(SimpleExtension extension) {
    return getSimpleExtensionClient().resource(extension).patchStatus();
  }

  public void deleteSimpleExtension(SimpleExtension extension) {
    getSimpleExtensionClient().resource(extension).delete();
  }

  public Guest getGuestByNameAndNamespace(String name, String namespace) {
    return getGuestClient()
        .inNamespace(namespace)
        .withName(name)
        .item();
  }

  public MixedOperation<Build, KubernetesResourceList<Build>, Resource<Build>> getBuildClient() {
    return client.resources(Build.class);
  }

  public MixedOperation<Service, KubernetesResourceList<Service>, Resource<Service>> getServiceClient() {
    return client.resources(Service.class);
  }

  public void createBuild(Build build) {
    getBuildClient().inNamespace(build.getMetadata().getNamespace()).resource(build).create();
  }

  public Build getBuildByNamespaceAndName(Build build) {
    return getBuildClient().inNamespace(build.getMetadata().getNamespace()).resource(build).get();
  }

  public Build getBuildByNamespaceAndName(String namespace, String name) {
    Build build = new Build();
    build.setMetaData(namespace, name);
    return getBuildClient().inNamespace(namespace).resource(build).get();
  }

  public MixedOperation<Deployment, KubernetesResourceList<Deployment>, Resource<Deployment>> getDeploymentClient() {
    return client.resources(Deployment.class);
  }
  public MixedOperation<Account, KubernetesResourceList<Account>, Resource<Account>> getAccountClient() {
    return client.resources(Account.class);
  }

  public void createDeployment(Deployment deployment) {
    getDeploymentClient().inNamespace(deployment.getMetadata().getNamespace()).resource(deployment).create();
  }

  public Deployment getDeploymentByNamespaceAndName(Deployment deployment) {
    return getDeploymentClient().inNamespace(deployment.getMetadata().getNamespace()).resource(deployment).get();
  }

  public Deployment getDeploymentByNamespaceAndName(String namespace, String name) {
    Deployment deployment = new Deployment();
    deployment.setMetaData(namespace, name);
    return getDeploymentClient().inNamespace(namespace).resource(deployment).get();
  }


}
