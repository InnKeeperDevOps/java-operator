package run.innkeeper.extensions.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.gatewayapi.v1alpha2.TCPRoute;
import io.fabric8.kubernetes.api.model.gatewayapi.v1alpha2.TCPRouteBuilder;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.BackendRef;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.BackendRefBuilder;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.Gateway;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.GatewayAddress;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.GatewayBuilder;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.ParentReference;

import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.RouteGroupKindBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import run.innkeeper.extensions.Extension;
import run.innkeeper.extensions.ExtensionStructure;
import run.innkeeper.extensions.gateway.dto.BridgeDetailDTO;
import run.innkeeper.extensions.gateway.dto.CreateNewRequestDTO;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionState;
import run.innkeeper.v1.simpleExtensions.crd.BackendProxySettings;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

@Extension("BackendProxy")
public class BackendProxy implements ExtensionStructure{
  K8sService k8sService = K8sService.get();

  public String gatewayAddress(SimpleExtension simpleExtension) {
    BackendProxySettings backendProxySettings = new BackendProxySettings(simpleExtension);
    String name = simpleExtension.getMetadata().getName();
    Gateway gateway = new GatewayBuilder()
        .withNewMetadata()
        .withName(name + "-gateway")
        .withNamespace(backendProxySettings.getNamespace())
        .endMetadata()
        .build();
    gateway = this.k8sService.getClient().resource(gateway).get();
    if (gateway != null) {
      Optional<GatewayAddress> optionalGatewayAddress = gateway.getStatus().getAddresses().stream().findFirst();
      if (optionalGatewayAddress.isPresent()) {
        return optionalGatewayAddress.get().getValue();
      }
    }
    return null;
  }

  CloseableHttpClient getClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    return HttpClients
        .custom()
        .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
        .build();
  }

  public void deleteBridge(SimpleExtension simpleExtension) throws URISyntaxException, IOException, InterruptedException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    BackendProxySettings backendProxySettings = new BackendProxySettings(simpleExtension);
    String name = simpleExtension.getMetadata().getName();
    String gateway = this.gatewayAddress(simpleExtension);
    if (gateway != null) {
      CloseableHttpClient httpClient = getClient();
      HttpUriRequest request = new HttpDelete(new URI(backendProxySettings.getBackendUri() + "/token/" + backendProxySettings.getNamespace() + "-" + name + "/"));
      request.setHeader("Bearer", backendProxySettings.getBackendToken());
      CloseableHttpResponse response = httpClient.execute(request);
      if (response.getStatusLine().getStatusCode() != 200) {
        String entity = EntityUtils.toString(response.getEntity());
        response.close();
        httpClient.close();
        Logging.error(entity);
      } else {
        response.close();
        httpClient.close();
      }
    }
  }

  public BridgeDetailDTO getBridge(SimpleExtension simpleExtension) throws URISyntaxException, IOException, InterruptedException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    BackendProxySettings backendProxySettings = new BackendProxySettings(simpleExtension);
    String name = simpleExtension.getMetadata().getName();
    String gateway = this.gatewayAddress(simpleExtension);
    if (gateway != null) {
      CloseableHttpClient httpClient = getClient();
      HttpUriRequest request = new HttpGet(new URI(backendProxySettings.getBackendUri() + "/token/" + backendProxySettings.getNamespace() + "-" + name + "/"));
      request.setHeader("Bearer", backendProxySettings.getBackendToken());
      CloseableHttpResponse response = httpClient.execute(request);
      if (response.getStatusLine().getStatusCode() == 200) {
        String entity = EntityUtils.toString(response.getEntity());
        response.close();
        httpClient.close();
        if (!entity.equals("")) {
          return new ObjectMapper().readValue(entity, BridgeDetailDTO.class);
        }
      } else {
        response.close();
        httpClient.close();
      }
    }
    return null;
  }

  public BridgeDetailDTO createBridge(SimpleExtension simpleExtension) throws URISyntaxException, IOException, InterruptedException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    BackendProxySettings backendProxySettings = new BackendProxySettings(simpleExtension);
    String name = simpleExtension.getMetadata().getName();
    String gateway = this.gatewayAddress(simpleExtension);
    if (gateway != null) {
      CreateNewRequestDTO createNewRequestDTO = new CreateNewRequestDTO();
      createNewRequestDTO.setName(backendProxySettings.getNamespace() + "-" + name);
      createNewRequestDTO.setBackendHost(new CreateNewRequestDTO.HostDetails(){{
        setPort(backendProxySettings.getServicePort().getIntVal());
        setAddress(gateway);
      }});
      createNewRequestDTO.setListenHost(new CreateNewRequestDTO.HostDetails(){{
        setAddress(backendProxySettings.getIp());
        setPort(backendProxySettings.getPort().getIntVal());
      }});
      CloseableHttpClient httpClient = getClient();
      HttpPost request = new HttpPost(new URI(backendProxySettings.getBackendUri() + "/token/"));
      request.setHeader("Bearer", backendProxySettings.getBackendToken());
      request.setHeader("Content-Type", "application/json");
      request.setEntity(
          new StringEntity(
              new ObjectMapper()
                  .writeValueAsString(
                      createNewRequestDTO
                  )
          )
      );
      CloseableHttpResponse response = httpClient.execute(request);
      if (response.getStatusLine().getStatusCode() != 200) {
        String entity = EntityUtils.toString(response.getEntity());
        response.close();
        httpClient.close();
        return new ObjectMapper().readValue(entity, BridgeDetailDTO.class);
      } else {
        response.close();
        httpClient.close();
      }
    }
    return null;
  }

  @Override
  public SimpleExtensionState create(SimpleExtension simpleExtension) {
    Gateway gatewayNew = this.buildGateway(simpleExtension);
    Gateway exists = k8sService.getClient().resource(gatewayNew).get();
    if (exists == null) {
      k8sService.getClient().resource(gatewayNew).create();
    }

    TCPRoute tcpRouteNew = this.buildTCPRoute(simpleExtension);
    TCPRoute tcpRouteOld = k8sService.getClient().resource(tcpRouteNew).get();
    if (tcpRouteOld == null) {
      k8sService.getClient().resource(tcpRouteNew).create();
    }

    try {
      BridgeDetailDTO bridgeDetailDTO = this.getBridge(simpleExtension);
      if (bridgeDetailDTO == null) {
        this.createBridge(simpleExtension);
      }
    } catch (URISyntaxException | IOException | InterruptedException | NoSuchAlgorithmException | KeyStoreException |
             KeyManagementException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public SimpleExtensionState update(SimpleExtension simpleExtension) {
    Logging.info("Simple extension checking....");
    Gateway gatewayNew = this.buildGateway(simpleExtension);
    Gateway gatewayOld = k8sService.getClient().resource(gatewayNew).get();
    if (gatewayOld == null) {
      k8sService.getClient().resource(gatewayNew).create();
    } else {
      gatewayOld.setSpec(gatewayNew.getSpec());
      k8sService.getClient().resource(gatewayOld).update();
    }
    TCPRoute tcpRouteNew = this.buildTCPRoute(simpleExtension);
    TCPRoute tcpRouteOld = k8sService.getClient().resource(tcpRouteNew).get();
    if (tcpRouteOld == null) {
      k8sService.getClient().resource(tcpRouteNew).create();
    } else {
      tcpRouteOld.setSpec(tcpRouteNew.getSpec());
      k8sService.getClient().resource(tcpRouteNew).update();
    }
    forceRefreshBP(simpleExtension);
    return SimpleExtensionState.UP_TO_DATE;
  }

  public void forceRefreshBP(SimpleExtension simpleExtension) {
    try {
      this.deleteBridge(simpleExtension);
      Thread.sleep(2000);
      this.createBridge(simpleExtension);
    } catch (URISyntaxException
             | IOException
             | InterruptedException
             | NoSuchAlgorithmException
             | KeyStoreException
             | KeyManagementException e) {
      e.printStackTrace();
    }
  }

  @Override
  public SimpleExtensionState check(SimpleExtension simpleExtension) {
    String gateway = gatewayAddress(simpleExtension);
    if (gateway != null) {
      BackendProxySettings backendProxySettings = new BackendProxySettings(simpleExtension);
      try {

        BridgeDetailDTO bridgeDetailDTO = this.getBridge(simpleExtension);
        if (bridgeDetailDTO != null) {
          if (
              !bridgeDetailDTO.getBackend().getAddress().equals(gateway) ||
                  bridgeDetailDTO.getBackend().getPort() != backendProxySettings.getServicePort().getIntVal() ||
                  bridgeDetailDTO.getPub().getPort() != backendProxySettings.getPort().getIntVal() ||
                  !bridgeDetailDTO.getPub().getAddress().equals(backendProxySettings.getIp())
          ) {
            forceRefreshBP(simpleExtension);
          } else {
            Logging.info("Nothing changed!");
          }
        } else {
          this.createBridge(simpleExtension);
        }
      } catch (URISyntaxException | IOException | InterruptedException | NoSuchAlgorithmException
               | KeyStoreException e) {
        e.printStackTrace();
      } catch (KeyManagementException e) {
        throw new RuntimeException(e);
      }
    }

    if (checkNeedToUpdate(simpleExtension)) {
      return SimpleExtensionState.NEED_TO_UPDATE;
    }
    return SimpleExtensionState.UP_TO_DATE;
  }

  private boolean checkNeedToUpdate(SimpleExtension simpleExtension) {
    BackendProxySettings backendProxySettings = new BackendProxySettings(simpleExtension);
    String name = simpleExtension.getMetadata().getName();
    Gateway gateway = this.k8sService.getClient().resource(buildGateway(simpleExtension)).get();
    TCPRoute tcpRoute = this.k8sService.getClient().resource(buildTCPRoute(simpleExtension)).get();
    if (gateway != null) {
      if (!Objects.equals(gateway.getSpec().getListeners().get(0).getPort(), backendProxySettings.getServicePort().getIntVal())) {
        return true;
      }
    } else {
      return true;
    }
    if (tcpRoute != null) {
      BackendRef backendRef = tcpRoute.getSpec().getRules().get(0).getBackendRefs().get(0);
      ParentReference parentReference = tcpRoute.getSpec().getParentRefs().get(0);
      if (!Objects.equals(backendRef.getPort(), backendProxySettings.getServicePort().getIntVal())) {
        return true;
      }
      if (!Objects.equals(backendRef.getName(), name)) {
        return true;
      }
      if (!Objects.equals(parentReference.getName(), name + "-gateway")) {
        return true;
      }
    } else {
      return true;
    }
    return false;
  }

  @Override
  public Object get(SimpleExtension simpleExtension) {
    Gateway gateway = this.buildGateway(simpleExtension);
    gateway = k8sService.getClient().resource(gateway).get();
    TCPRoute tcpRoute = this.buildTCPRoute(simpleExtension);
    tcpRoute = k8sService.getClient().resource(tcpRoute).get();
    try {
      return new Object[]{gateway, tcpRoute, this.getBridge(simpleExtension)};
    } catch (URISyntaxException | IOException | InterruptedException | NoSuchAlgorithmException | KeyStoreException |
             KeyManagementException e) {
      e.printStackTrace();
    }
    return new Object[]{gateway, tcpRoute, null};
  }

  @Override
  public void delete(SimpleExtension simpleExtension) {
    Gateway gateway = this.buildGateway(simpleExtension);
    k8sService.getClient().resource(gateway).delete();
    TCPRoute tcpRoute = this.buildTCPRoute(simpleExtension);
    k8sService.getClient().resource(tcpRoute).delete();
  }

  public TCPRoute buildTCPRoute(SimpleExtension simpleExtension) {
    String name = simpleExtension.getMetadata().getName();
    BackendProxySettings backendProxySettings = new BackendProxySettings(simpleExtension);
    return new TCPRouteBuilder()
        .withNewMetadata()
        .withName(name)
        .withNamespace(backendProxySettings.getNamespace())
        .endMetadata()
        .withNewSpec()
        .addNewParentRef()
        .withName(name + "-gateway")
        .withPort(backendProxySettings.getServicePort().getIntVal())
        .withSectionName(name)
        .endParentRef()
        .addNewRule()
        .addToBackendRefs(
            new BackendRefBuilder()
                .withName(backendProxySettings.getService())
                .withPort(backendProxySettings.getServicePort().getIntVal())
                .withKind("Service")
                .withNamespace(backendProxySettings.getNamespace())
                .build()
        )
        .endRule()
        .endSpec()
        .build();
  }

  public Gateway buildGateway(SimpleExtension simpleExtension) {
    String name = simpleExtension.getMetadata().getName();
    BackendProxySettings backendProxySettings = new BackendProxySettings(simpleExtension);
    return new GatewayBuilder()
        .withNewMetadata()
        .withName(name + "-gateway")
        .withNamespace(backendProxySettings.getNamespace())
        .endMetadata()
        .withNewSpec()
        .withGatewayClassName("istio")
        .addNewListener()
        .withName(name)
        .withProtocol("TCP")
        .withPort(backendProxySettings.getServicePort().getIntVal())
        .endListener()
        .endSpec()
        .build();
  }
}
