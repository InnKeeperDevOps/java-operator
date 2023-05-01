package run.innkeeper.v1.guest.crd.objects;

import io.fabric8.generator.annotation.Required;
import run.innkeeper.v1.guest.crd.objects.network.TrafficType;

import java.util.HashMap;
import java.util.Map;

public class TrafficSettings {
    @Required
    TrafficType type;
    @Required
    String name;
    Map<String, String> meta = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TrafficType getType() {
        return type;
    }

    public void setType(TrafficType type) {
        this.type = type;
    }
}
