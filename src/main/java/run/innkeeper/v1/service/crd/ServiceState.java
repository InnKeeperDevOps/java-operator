package run.innkeeper.v1.service.crd;

public enum ServiceState {
    CREATING("CG"),
    CREATED("CD"),
    NEED_TO_CREATE("NTC"),
    RECREATE("RC"),
    FAILED("F");

    String value;

    ServiceState(String value) {
        this.value = value;
    }
}
