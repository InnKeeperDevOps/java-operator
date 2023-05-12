package run.innkeeper.v1.deployment.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;
import run.innkeeper.utilities.HashGenerator;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;

@Group("cicd.innkeeper.run")
@Version("v1")
@ShortNames("deploy")
public class Deployment extends CustomResource<DeploymentSpec, DeploymentStatus> implements
    Namespaced {

    public Deployment setMetaData(String namespace, String name){
        ObjectMeta om = new ObjectMeta();
        om.setNamespace(namespace);
        om.setName(name);
        this.setMetadata(om);
        return this;
    }
}
