package run.innkeeper.api.dto.ws;

import java.io.Serializable;

public class RelayCommand implements Serializable{
  String uri;
  String body;
  String type;

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
