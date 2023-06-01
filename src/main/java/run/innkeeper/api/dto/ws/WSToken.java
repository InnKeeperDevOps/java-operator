package run.innkeeper.api.dto.ws;

public class WSToken{
  String token;

  String email;

  public WSToken(String token, String email) {
    this.token = token;
    this.email = email;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
