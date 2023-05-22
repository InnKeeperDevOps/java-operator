package run.innkeeper.v1.account.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.build.crd.BuildSpec;
import run.innkeeper.v1.build.crd.BuildStatus;

@Group("cicd.innkeeper.run")
@Version("v1")
@ShortNames("account")
public class Account extends CustomResource<AccountSpec, AccountStatus> implements Namespaced{
  /**
   * Sets meta data.
   *
   * @param namespace the namespace
   * @param name      the name
   * @return the meta data
   */
  public Account setMetaData(String namespace, String name) {
    ObjectMeta om = new ObjectMeta();
    om.setNamespace(namespace);
    om.setName(name);
    this.setMetadata(om);
    return this;
  }
}