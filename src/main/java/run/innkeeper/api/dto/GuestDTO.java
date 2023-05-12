package run.innkeeper.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.guest.crd.GuestSpec;
import run.innkeeper.v1.guest.crd.GuestStatus;

import java.io.Serializable;

public class GuestDTO {
    @JsonProperty("spec")
    GuestSpec spec;
    @JsonProperty("status")
    GuestStatus status;

    ObjectMetaDTO meta;

    public GuestDTO(Guest guest) {
        this.spec = guest.getSpec();
        this.status = guest.getStatus();
        this.meta = new ObjectMetaDTO(guest.getMetadata());
    }

    public GuestSpec getSpec() {
        return spec;
    }

    public void setSpec(GuestSpec spec) {
        this.spec = spec;
    }

    public GuestStatus getStatus() {
        return status;
    }

    public void setStatus(GuestStatus status) {
        this.status = status;
    }

    public ObjectMetaDTO getMeta() {
        return meta;
    }

    public void setMeta(ObjectMetaDTO meta) {
        this.meta = meta;
    }
}
