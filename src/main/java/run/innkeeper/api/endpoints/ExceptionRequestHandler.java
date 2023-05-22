package run.innkeeper.api.endpoints;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import run.innkeeper.api.auth.UnauthorizedException;
import run.innkeeper.utilities.Logging;

/**
 * The type Exception request handler.
 */
@ControllerAdvice
public class ExceptionRequestHandler extends ResponseEntityExceptionHandler{
  /**
   * Handle unauthorized request response entity.
   *
   * @param ex      the ex
   * @param request the request
   * @return the response entity
   */
  @ExceptionHandler(UnauthorizedException.class)
  protected ResponseEntity<Object> handleUnauthorizedRequest(RuntimeException ex, WebRequest request) {
    Logging.error(ex.getCause().getMessage());
    return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
  }
}