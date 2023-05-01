package run.innkeeper.v1.guest.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("cicd.innkeeper.run")
@Version("v1")
@ShortNames("guest")
public class Guest extends CustomResource<GuestSpec, GuestStatus> implements
    Namespaced {
}
