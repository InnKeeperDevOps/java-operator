package run.innkeeper.v1.guest.crd.objects.build;

import io.fabric8.generator.annotation.Required;

public class GitSource{
  @Required
  String uri;
  @Required
  String secret;
  String commit;
  String branch;

  public GitSource(GitSource old) {
    this.secret = old.secret;
    this.commit = old.commit;
    this.branch = old.branch;
    this.uri = old.uri;
  }

  public GitSource() {
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public String getCommit() {
    return commit;
  }

  public void setCommit(String commit) {
    this.commit = commit;
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }
}
