package run.innkeeper.api.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.annotations.UserAuthorized;
import run.innkeeper.api.services.AdminUIFileSystem;

@RestController
public class IndexController{
  @Autowired
  AdminUIFileSystem adminUIFileSystem;

  @GetMapping("/")
  @UserAuthorized("server.index")
  public String getIndex() {
    return adminUIFileSystem.getFile("index.html");
  }
}
