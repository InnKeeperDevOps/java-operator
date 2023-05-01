package run.innkeeper.v1.guest.crd.objects.network;

public enum TrafficType {
    ISTIO_HTTP("ISTIO_HTTP");
    String value;
    TrafficType(String val) {
        this.value = val;
    }
}
