package run.innkeeper.api.endpoints;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import run.innkeeper.api.auth.UnauthorizedException;

@ControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler{
  @ExceptionHandler(UnauthorizedException.class)
  protected ResponseEntity<Object> handleConflict(
      RuntimeException ex, WebRequest request) {
    new ExceptionHandler();
    String bodyOfResponse = "This should be application specific";
    return handleExceptionInternal(ex, bodyOfResponse,new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
  }
}