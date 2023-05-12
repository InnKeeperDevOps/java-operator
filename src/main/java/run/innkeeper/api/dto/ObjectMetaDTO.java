package run.innkeeper.api.dto;

import io.fabric8.kubernetes.api.model.ObjectMeta;

public class ObjectMetaDTO {
    String name;
    String namespace;
    String uuid;

    public ObjectMetaDTO(ObjectMeta om ) {
        this.name = om.getName();
        this.namespace = om.getNamespace();
        this.uuid = om.getUid();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
