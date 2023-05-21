package run.innkeeper.api.auth;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import run.innkeeper.utilities.Logging;

@Aspect
@Component
public class CheckUserAuthorized {

  @Before("@annotation(UserAuthorized)")
  public void checkUserValid() throws UnauthorizedException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      Logging.error(authentication.getPrincipal());
      String username = authentication.getName();
      // Perform your user check logic here
      if (!isUserValid(username)) {
        throw new UnauthorizedException("User is not valid"); // Throw an exception or handle the unauthorized user
      }
    } else {
      throw new UnauthorizedException("User is not authenticated");
    }
  }

  private boolean isUserValid(String username) {
    // Your custom logic to check if the user is valid
    // Return true if the user is valid; otherwise, return false
    // You can check against your user repository or any other validation mechanism
    // Example: return userRepository.existsByUsername(username);
    return false;
  }
}