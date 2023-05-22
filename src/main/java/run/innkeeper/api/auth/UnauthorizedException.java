package run.innkeeper.api.auth;

public class UnauthorizedException extends Exception{
  public UnauthorizedException(String message) {
    super(message);
  }
}
