package run.innkeeper.jobs;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class LatestCommitCheck extends JobStructure {

  public LatestCommitCheck() {
    this(UUID.randomUUID().toString().replaceAll("-","").substring(0,16));
  }
  public LatestCommitCheck(String name) {
    super(
        "glc-"+ name,
        "ghcr.io/innkeeperdevops/git-latest:12",
        new LinkedHashMap<>(){{
          this.put("GIT_REPO", "https://github.com/InnKeeperDevOps/admin-ui.git");
        }}
    );
  }

  public static LatestCommitCheck newInstance(String name){
    return new LatestCommitCheck(name);
  }

  public static LatestCommitCheck newInstance(){
    return new LatestCommitCheck();
  }

}
