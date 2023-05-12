package run.innkeeper.v1.build.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;
import run.innkeeper.utilities.HashGenerator;
import run.innkeeper.v1.deployment.crd.Deployment;


/**
 * The type Build.
 */
@Group("cicd.innkeeper.run")
@Version("v1")
@ShortNames("build")
public class Build extends CustomResource<BuildSpec, BuildStatus> implements Namespaced {
  /**
   * Sets meta data.
   *
   * @param namespace the namespace
   * @param name      the name
   * @return the meta data
   */
  public Build setMetaData(String namespace, String name) {
    ObjectMeta om = new ObjectMeta();
    om.setNamespace(namespace);
    om.setName(name);
    this.setMetadata(om);
    return this;
  }
}
