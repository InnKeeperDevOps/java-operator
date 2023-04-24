package run.innkeeper.v1.guest.crd.objects.build;

import io.fabric8.generator.annotation.Required;

public class Publish {
    @Required
    String secret;
    @Required
    String registry;
    @Required
    String tag;

    public Publish(Publish old) {
        this.secret = old.secret;
        this.registry = old.registry;
        this.tag = old.tag;
    }

    public Publish() {
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }


}
