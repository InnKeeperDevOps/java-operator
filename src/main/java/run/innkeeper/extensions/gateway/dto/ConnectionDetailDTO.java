package run.innkeeper.extensions.gateway.dto;

import java.io.Serializable;

public class ConnectionDetailDTO implements Serializable{
  private String address = "";
  private int port = 0;

  public ConnectionDetailDTO() {
  }

  public ConnectionDetailDTO(String address, int port) {
    this.address = address;
    this.port = port;
  }

  public String getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setPort(int port) {
    this.port = port;
  }
}
