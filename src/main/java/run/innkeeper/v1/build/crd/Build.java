package run.innkeeper.v1.build.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;
import run.innkeeper.utilities.HashGenerator;

import java.math.BigInteger;
import java.security.MessageDigest;

@Group("cicd.innkeeper.run")
@Version("v1")
@ShortNames("build")
public class Build extends CustomResource<BuildSpec, BuildStatus> implements Namespaced {
    public void setMetaData(String namespace, String name){
        ObjectMeta om = new ObjectMeta();
        om.setNamespace(namespace);
        om.setName(HashGenerator.getBuildName(namespace, name));
        this.setMetadata(om);
    }

}
