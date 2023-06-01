package run.innkeeper.jobs;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class LatestCommitCheck extends JobStructure {

  public LatestCommitCheck() {
    super(
    "glc-"+ UUID.randomUUID().toString().replaceAll("-","").substring(0,16),
    "ghcr.io/innkeeperdevops/git-latest:11",
    new LinkedHashMap<>(){{
      this.put("GIT_REPO", "https://github.com/InnKeeperDevOps/admin-ui.git");
    }}
    );
  }

  public static LatestCommitCheck newInstance(){
    return new LatestCommitCheck();
  }

}
