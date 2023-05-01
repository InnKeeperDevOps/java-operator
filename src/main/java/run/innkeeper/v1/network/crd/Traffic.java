package run.innkeeper.v1.network.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("cicd.innkeeper.run")
@Version("v1")
@ShortNames("traffic")
public class Traffic extends CustomResource<TrafficSpec, TrafficStatus> implements
    Namespaced {
    public void setMetaData(String namespace, String name){
        ObjectMeta om = new ObjectMeta();
        om.setNamespace(namespace);
        om.setName(name);
        this.setMetadata(om);
    }

}
