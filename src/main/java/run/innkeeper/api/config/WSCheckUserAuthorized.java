package run.innkeeper.api.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import run.innkeeper.api.annotations.WebSocketUserAuthorized;
import run.innkeeper.api.dto.ws.WSSession;
import run.innkeeper.api.services.WSSessionStorage;
import run.innkeeper.services.AccountService;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.account.crd.Account;

import java.security.Principal;
import java.util.Arrays;

@Aspect
@Component
public class WSCheckUserAuthorized{
  AccountService accountService = AccountService.get();

  @Autowired
  WSSessionStorage wsSessionStorage;

  @Around("@annotation(run.innkeeper.api.annotations.WebSocketUserAuthorized)")
  public Object checkUserValid(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    if(System.getenv("NO_AUTH") != null){
      return proceedingJoinPoint.proceed();
    }
    Object[] args = proceedingJoinPoint.getArgs();
    OAuth2AuthenticationToken principal = null;
    SimpMessageHeaderAccessor headerAccessor = null;
    for (Object arg : args) {
      if (arg instanceof Principal) {
        principal = (OAuth2AuthenticationToken) arg;
      } else if (arg instanceof SimpMessageHeaderAccessor) {
        headerAccessor = (SimpMessageHeaderAccessor) arg;
      }
    }
    String sessionId = headerAccessor.getSessionId();
    WSSession wsSession = this.wsSessionStorage.getSessions().get(sessionId);
    if (wsSession == null) {
      throw new UnauthorizedException("session not found!");
    }
    MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
    WebSocketUserAuthorized userAuthorized = signature.getMethod().getAnnotation(WebSocketUserAuthorized.class);

    if (principal.getPrincipal() instanceof DefaultOidcUser) {
      Account account = accountService.getAccount(((DefaultOidcUser) principal.getPrincipal()));
      if (account == null && accountService.countAccounts() == 0) {
        // create admin account
        account = accountService.createAccount(
            ((DefaultOidcUser) principal.getPrincipal()),
            Arrays.asList(
                "**"
            )
        );
      } else if (account == null) {
        account = accountService.createAccount(
            ((DefaultOidcUser) principal.getPrincipal()),
            Arrays.asList()
        );
      }
      // Perform your user check logic here
      if (account == null || !accountService.hasPermission(account.getMetadata().getName(), userAuthorized.value())) {
        if (account != null) {
          throw new UnauthorizedException(account.getMetadata().getName() + " denied access to permission \"" + userAuthorized.value() + "\" to path "); // Throw an exception or handle the unauthorized user
        } else {
          throw new UnauthorizedException("unknown denied access to permission \"" + userAuthorized.value() + "\" to path ");
        }
      }
    }
    return proceedingJoinPoint.proceed();
  }
}
