package run.innkeeper.v1.simpleExtensions.crd;

public enum SimpleExtensionState {
    UP_TO_DATE("UTD"),
    NEED_TO_UPDATE("NTU"),
    NEED_TO_CREATE("NTU");

    String value;

    SimpleExtensionState(String value) {
        this.value = value;
    }

}
