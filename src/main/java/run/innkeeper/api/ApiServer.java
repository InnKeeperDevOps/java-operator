package run.innkeeper.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
@EnableWebSocket
@EnableAspectJAutoProxy
public class ApiServer {
  public ApiServer() throws IOException {

  }


}
