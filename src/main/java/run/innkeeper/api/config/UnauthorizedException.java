package run.innkeeper.api.config;

public class UnauthorizedException extends Exception{
  public UnauthorizedException(String message) {
    super(message);
  }
}
