package run.innkeeper.api.endpoints.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import run.innkeeper.api.annotations.WebSocketUserAuthorized;
import run.innkeeper.api.dto.ws.RelayCommand;
import run.innkeeper.api.dto.ws.WSSession;
import run.innkeeper.api.dto.ws.WSToken;
import run.innkeeper.api.services.WSSessionStorage;
import run.innkeeper.utilities.Logging;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class WebSocketServer {

  @Autowired
  WSSessionStorage wsSessionStorage;

  private RestTemplate restTemplate = new RestTemplate();

  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;

  @MessageMapping("/command")
  @SendToUser("/client/command")
  @WebSocketUserAuthorized("ws.command")
  public String onMessage(@Payload RelayCommand relayCommand, SimpMessageHeaderAccessor headerAccessor, Principal principal) throws ExecutionException {
    WSSession wsSession = wsSessionStorage.getSessions().get(headerAccessor.getSessionId());
    String url = "http://127.0.0.1:8081"+relayCommand.getUri();
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", "JSESSIONID="+wsSession.getToken());
    HttpEntity<String> requestEntity = new HttpEntity<>(relayCommand.getBody(), headers);

    ResponseEntity<String> apiResult = null;
    switch(relayCommand.getType().toLowerCase()){
      case "get":
        apiResult = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        break;
      case "post":
        apiResult = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        break;
      case "put":
        apiResult = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
        break;
    }
    return apiResult.getBody();
  }

  @MessageMapping("/connect")
  public void onConnect(@Payload List<String> tokens, SimpMessageHeaderAccessor headerAccessor) throws ExecutionException {
    String uuid = tokens.get(0);
    String sessionId = headerAccessor.getSessionId();
    WSToken wsToken = wsSessionStorage.getCache().get(uuid);
    if(wsToken.getToken().equals(tokens.get(1))){
      wsSessionStorage.getCache().invalidate(uuid);
      wsSessionStorage.getSessions().put(sessionId, new WSSession(wsToken.getEmail(), wsToken.getToken()));
    }
  }

}