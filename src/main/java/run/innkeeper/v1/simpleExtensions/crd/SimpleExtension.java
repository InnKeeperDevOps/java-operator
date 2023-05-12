package run.innkeeper.v1.simpleExtensions.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;
import run.innkeeper.v1.service.crd.ServiceSpec;
import run.innkeeper.v1.service.crd.ServiceStatus;

@Group("cicd.innkeeper.run")
@Version("v1")
@ShortNames("se")
public class SimpleExtension extends CustomResource<SimpleExtensionSpec, SimpleExtensionStatus> implements
    Namespaced {
    public SimpleExtension setMetaData(String namespace, String name){
        ObjectMeta om = new ObjectMeta();
        om.setNamespace(namespace);
        om.setName(name);
        this.setMetadata(om);
        return this;
    }
}
