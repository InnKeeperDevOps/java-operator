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

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Exception request handler.
 */
@ControllerAdvice
public class ExceptionRequestHandler extends ResponseEntityExceptionHandler{
  class EventLog {
     Instant time;
     String message;

    public EventLog(String message) {
      this.time = Instant.now();
      this.message = message;
    }

    public Instant getTime() {
      return time;
    }

    public String getMessage() {
      return message;
    }
  }
  public static List<EventLog> permissionErrors = new ArrayList<>();
  public static List<EventLog> ioErrors = new ArrayList<>();

  /**
   * Handle unauthorized request response entity.
   *
   * @param ex      the ex
   * @param request the request
   * @return the response entity
   */
  @ExceptionHandler(UnauthorizedException.class)
  protected ResponseEntity<Object> handleUnauthorizedRequest(RuntimeException ex, WebRequest request) {
    permissionErrors.add(new EventLog(ex.getCause().getMessage()));
    Logging.error(ex.getCause().getMessage());
    return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
  }
  @ExceptionHandler(IOException.class)
  protected ResponseEntity<Object> handleErrorRequest(IOException ex, WebRequest request) {
    ioErrors.add(new EventLog(ex.getCause().getMessage()));
    Logging.error(ex.getCause().getMessage());
    return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  public static List<EventLog> getPermissionErrors() {
    return permissionErrors;
  }

  public static List<EventLog> getIoErrors() {
    return ioErrors;
  }
}