package run.innkeeper.v1.simpleExtensions.crd;

import io.fabric8.generator.annotation.Required;
import io.fabric8.kubernetes.api.model.IntOrString;

import java.util.HashMap;
import java.util.Map;

public class SimpleExtensionSpec {
    @Required
    String type;

    @Required
    String name;

    @Required
    Map<String, IntOrString> data = new HashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, IntOrString> getData() {
        return data;
    }

    public void setData(Map<String, IntOrString> data) {
        this.data = data;
    }
}
