package run.innkeeper.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.auth.UserAuthorized;

@RestController
public class IndexController{
  @GetMapping("/")
  @UserAuthorized("server.docs")
  public String getIndex() {
    return "redirect:/docs";
  }
}
