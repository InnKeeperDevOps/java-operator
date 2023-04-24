package run.innkeeper.v1.guest.crd.objects.build;

import io.fabric8.generator.annotation.Required;

public class Docker {
    @Required
    String dockerfile;
    String workdir;

    public Docker(Docker old) {
        this.dockerfile = old.dockerfile;
        this.workdir = old.workdir;
    }

    public Docker() {
    }

    public String getDockerfile() {
        return dockerfile;
    }

    public void setDockerfile(String dockerfile) {
        this.dockerfile = dockerfile;
    }

    public String getWorkdir() {
        return workdir;
    }

    public void setWorkdir(String workdir) {
        this.workdir = workdir;
    }
}
