package run.innkeeper.api.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import run.innkeeper.api.dto.ws.WSSession;
import run.innkeeper.api.dto.ws.WSToken;
import run.innkeeper.api.services.WSSessionStorage;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class WebSocketConnect{
  SimpUserRegistry simpUserRegistry;
  public WebSocketConnect(SimpUserRegistry simpUserRegistry) {
    this.simpUserRegistry = simpUserRegistry;
  }

  @Autowired
  WSSessionStorage wsSessionStorage;

  @MessageMapping("/connect")
  @SendToUser("/client/connected")
  public Object onConnect(@Payload List<String> tokens, SimpMessageHeaderAccessor headerAccessor) throws ExecutionException {
    String uuid = tokens.get(0);
    String sessionId = headerAccessor.getSessionId();
    WSToken wsToken = wsSessionStorage.getCache().get(uuid);
    if(wsToken.getToken().equals(tokens.get(1))){
      wsSessionStorage.getCache().invalidate(uuid);
      wsSessionStorage.getSessions().put(sessionId, new WSSession(wsToken.getEmail(), wsToken.getToken()));
    }
    return "ok";
  }
}
