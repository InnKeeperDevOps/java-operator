package run.innkeeper.api.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.annotations.UserAuthorized;
import run.innkeeper.api.dto.ws.WSToken;
import run.innkeeper.api.services.WSSessionStorage;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ws")
public class WebSocketAuthToken{

  @Autowired
  WSSessionStorage wsSessionStorage;

  @GetMapping("/token")
  @UserAuthorized("ws.token")
  public List<String> getToken(OAuth2AuthenticationToken principal, @CookieValue(value = "JSESSIONID", required = false, defaultValue = "test") String sessionId) {
    String email = UUID.randomUUID().toString();
    if (principal != null && principal.getPrincipal() != null) {
      ((DefaultOidcUser) principal.getPrincipal()).getEmail();
    }
    String uuid = UUID.randomUUID().toString();
    wsSessionStorage.getCache().put(uuid, new WSToken(sessionId, email));
    return Arrays.asList(uuid, sessionId);
  }
}
