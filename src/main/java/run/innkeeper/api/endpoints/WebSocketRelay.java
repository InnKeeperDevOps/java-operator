package run.innkeeper.api.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import run.innkeeper.api.annotations.WebSocketUserAuthorized;
import run.innkeeper.api.dto.ws.RelayCommand;
import run.innkeeper.api.dto.ws.WSSession;
import run.innkeeper.api.dto.ws.WSToken;
import run.innkeeper.api.services.WSSessionStorage;
import run.innkeeper.utilities.Logging;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class WebSocketRelay{

  @Autowired private SimpUserRegistry simpUserRegistry;


  @Autowired
  WSSessionStorage wsSessionStorage;

  private RestTemplate restTemplate = new RestTemplate();

  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;

  @MessageMapping("/command")
  @WebSocketUserAuthorized("ws.command")
  @SendToUser("/client/command")
  public Object onMessage(@Payload RelayCommand relayCommand, SimpMessageHeaderAccessor headerAccessor, Principal principal) throws ExecutionException, IOException {
    String sessionId = headerAccessor.getSessionId();
    WSSession wsSession = wsSessionStorage.getSessions().get(sessionId);
    String url = "http://127.0.0.1:8081" + relayCommand.getUri();
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", "JSESSIONID=" + wsSession.getToken());
    HttpEntity<String> requestEntity = new HttpEntity<>(relayCommand.getBody(), headers);
    ResponseEntity<String> apiResult = switch (relayCommand.getType().toLowerCase()) {
      case "get" -> restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
      case "post" -> restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
      case "put" -> restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
      default -> null;
    };
    return apiResult.getBody();
  }


}