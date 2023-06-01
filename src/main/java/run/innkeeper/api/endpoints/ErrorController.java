package run.innkeeper.api.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.annotations.UserAuthorized;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/error")
public class ErrorController{
  @GetMapping("/permission")
  @UserAuthorized("error.permission")
  public List<ExceptionRequestHandler.EventLog> getPermissionErrors() {
    return ExceptionRequestHandler
        .getPermissionErrors()
        .stream()
        .sorted(Comparator.comparing(ExceptionRequestHandler.EventLog::getTime).reversed())
        .limit(20)
        .collect(Collectors.toList());
  }

  @GetMapping("/io")
  @UserAuthorized("error.io")
  public List<ExceptionRequestHandler.EventLog> getIOErrors() {
    return ExceptionRequestHandler
        .getIoErrors()
        .stream()
        .sorted(Comparator.comparing(ExceptionRequestHandler.EventLog::getTime).reversed())
        .limit(20)
        .collect(Collectors.toList());
  }
}
