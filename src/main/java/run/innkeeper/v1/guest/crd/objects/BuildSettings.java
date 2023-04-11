package run.innkeeper.v1.guest.crd.objects;

import run.innkeeper.v1.guest.crd.objects.build.Docker;
import run.innkeeper.v1.guest.crd.objects.build.GitSource;
import run.innkeeper.v1.guest.crd.objects.build.Publish;
import io.fabric8.generator.annotation.Required;

public class BuildSettings {
    Docker docker;
    @Required
    GitSource git;
    @Required
    Publish publish;
    @Required
    String name;
    @Required
    String namespace;

    public Docker getDocker() {
        return docker;
    }

    public void setDocker(Docker docker) {
        this.docker = docker;
    }

    public GitSource getGit() {
        return git;
    }

    public void setGit(GitSource git) {
        this.git = git;
    }

    public Publish getPublish() {
        return publish;
    }

    public void setPublish(Publish publish) {
        this.publish = publish;
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
}
