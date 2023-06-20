package run.innkeeper.api.endpoints;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.annotations.UserAuthorized;
import run.innkeeper.api.services.AdminUIFileSystem;
import run.innkeeper.utilities.Logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class IndexController{
  @Autowired
  AdminUIFileSystem adminUIFileSystem;

  @GetMapping("/ui/**")
  @UserAuthorized("server.ui")
  public void getIndex(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String requestUri = request.getRequestURI().replaceFirst("/ui/", "");
    Path filePath = Paths.get(requestUri);
    String mimeType = Files.probeContentType(filePath);
    if(requestUri.isEmpty()){
      requestUri = "index.html";
    }
    String data = adminUIFileSystem.getFile(requestUri);
    if(data!=null) {
      response.addHeader("Content-Type", mimeType);
      response.getWriter().write(data);
    }else{
      response.getWriter().write("");
    }
  }
}
