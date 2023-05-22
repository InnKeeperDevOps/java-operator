package run.innkeeper.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import run.innkeeper.services.AccountService;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.account.crd.Account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class CheckUserAuthorized{
  AccountService accountService = AccountService.get();

  @Around("@annotation(UserAuthorized)")
  public Object checkUserValid(ProceedingJoinPoint pjp) throws Throwable {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    MethodSignature signature = (MethodSignature) pjp.getSignature();
    UserAuthorized userAuthorized = signature.getMethod().getAnnotation(UserAuthorized.class);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.isAuthenticated()) {
      Object principal = authentication.getPrincipal();
      if (principal instanceof DefaultOidcUser) {
        Account account = accountService.getAccount(((DefaultOidcUser) principal));
        if (account == null && accountService.countAccounts() == 0) {
          // create admin account
          account = accountService.createAccount(
              ((DefaultOidcUser) principal),
              Arrays.asList(
                  "**"
              )
          );
        } else if (account == null) {
          account = accountService.createAccount(
              ((DefaultOidcUser) principal),
              Arrays.asList()
          );
        }
        // Perform your user check logic here
        if (account == null || !accountService.hasPermission(account.getMetadata().getName(), userAuthorized.value())) {
          if(account!=null) {
            throw new UnauthorizedException(account.getMetadata().getName() + " denied access to permission \"" + userAuthorized.value() + "\" to path " + request.getRequestURI()); // Throw an exception or handle the unauthorized user
          }else{
            throw new UnauthorizedException("unknown denied access to permission \"" + userAuthorized.value() + "\" to path " + request.getRequestURI());
          }
        }
      }
    } else {
      throw new UnauthorizedException("User is not authenticated");
    }
    return pjp.proceed();
  }
}