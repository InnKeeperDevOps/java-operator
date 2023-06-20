package run.innkeeper.extensions.gateway.dto;


public class CreateNewRequestDTO{
  public static class HostDetails {
    private String address;
    private int port;
    public HostDetails() {
    }
    public String getAddress() {
      return address;
    }
    public void setAddress(String address) {
      this.address = address;
    }
    public int getPort() {
      return port;
    }
    public void setPort(int port) {
      this.port = port;
    }
  }
  private HostDetails listenHost;
  private HostDetails backendHost;
  private String name;
  public CreateNewRequestDTO() {
  }
  public HostDetails getListenHost() {
    return listenHost;
  }
  public void setListenHost(HostDetails listenHost) {
    this.listenHost = listenHost;
  }
  public HostDetails getBackendHost() {
    return backendHost;
  }
  public void setBackendHost(HostDetails backendHost) {
    this.backendHost = backendHost;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
