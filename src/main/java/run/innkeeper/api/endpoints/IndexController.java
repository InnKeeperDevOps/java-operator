package run.innkeeper.api.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.auth.UserAuthorized;
import run.innkeeper.services.AdminUIFileSystem;

@RestController
public class IndexController{
  AdminUIFileSystem adminUIFileSystem = AdminUIFileSystem.get();
  @GetMapping("/")
  @UserAuthorized("server.index")
  public String getIndex() {
    return adminUIFileSystem.getFiles().get("admin-ui-main/index.html");
  }
}
