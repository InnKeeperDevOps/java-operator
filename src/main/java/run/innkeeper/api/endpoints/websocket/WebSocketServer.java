package run.innkeeper.api.endpoints.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestTemplate;
import run.innkeeper.utilities.Logging;

@Controller
public class WebSocketServer {

  private RestTemplate restTemplate = new RestTemplate();

  @MessageMapping("/api")
  @SendTo("/api")
  public String onMessage(String message, Session session) {
    Logging.error(session);
    Logging.error(message);
    String apiResult = restTemplate.getForObject("https://localhost:8081/"+message, String.class);
    return apiResult;
  }


}