package run.innkeeper.v1.simpleExtensions.crd;

import io.fabric8.kubernetes.api.model.IntOrString;

public class BackendProxySettings{
  String backendUri;
  String backendToken;
  String service;
  String namespace;
  IntOrString servicePort;
  IntOrString port;
  String ip;

  public BackendProxySettings(SimpleExtension simpleExtension) {
    this.backendUri = simpleExtension.getSpec().getData().get("backendUri").getStrVal();
    this.backendToken = simpleExtension.getSpec().getData().get("backendToken").getStrVal();
    this.service = simpleExtension.getSpec().getData().get("service").getStrVal();
    this.namespace = simpleExtension.getSpec().getData().get("namespace").getStrVal();
    this.servicePort = simpleExtension.getSpec().getData().get("servicePort");
    this.port = simpleExtension.getSpec().getData().get("port");
    this.ip = simpleExtension.getSpec().getData().get("ip").getStrVal();
  }

  public String getBackendUri() {
    return backendUri;
  }

  public void setBackendUri(String backendUri) {
    this.backendUri = backendUri;
  }

  public String getBackendToken() {
    return backendToken;
  }

  public void setBackendToken(String backendToken) {
    this.backendToken = backendToken;
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public IntOrString getServicePort() {
    return servicePort;
  }

  public void setServicePort(IntOrString servicePort) {
    this.servicePort = servicePort;
  }

  public IntOrString getPort() {
    return port;
  }

  public void setPort(IntOrString port) {
    this.port = port;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }
}