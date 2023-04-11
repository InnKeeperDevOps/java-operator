package run.innkeeper.v1.guest.crd.objects.ingress;

public enum IngressType {
    HTTP("HTTP");
    String value;
    IngressType(String val) {
        this.value = val;
    }
}
