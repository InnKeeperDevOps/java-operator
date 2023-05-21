package run.innkeeper.api.auth;

public class UnauthorizedException extends Exception{
  String message;

  public UnauthorizedException(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
