package run.innkeeper.extensions.gateway.dto;

import java.io.Serializable;

public class BridgeDetailDTO implements Serializable {
  private ConnectionDetailDTO pub;
  private ConnectionDetailDTO backend;

  public BridgeDetailDTO() {
  }

  public BridgeDetailDTO(ConnectionDetailDTO pub, ConnectionDetailDTO backend) {
    this.pub = pub;
    this.backend = backend;
  }

  public ConnectionDetailDTO getPub() {
    return pub;
  }

  public ConnectionDetailDTO getBackend() {
    return backend;
  }

  public void setPub(ConnectionDetailDTO pub) {
    this.pub = pub;
  }

  public void setBackend(ConnectionDetailDTO backend) {
    this.backend = backend;
  }
}
