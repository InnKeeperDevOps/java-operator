package run.innkeeper.v1.simpleExtensions.crd;

public enum SimpleExtensionState {
    UP_TO_DATE("UTD"),
    NEED_TO_UPDATE("NTU"),
    STEP_ONE("STEP_ONE"),
    STEP_TWO("STEP_TWO"),
    STEP_THREE("STEP_THREE"),
    STEP_FOUR("STEP_FOUR"),
    STEP_FIVE("STEP_FIVE"),
    NEED_TO_CREATE("NTU");

    String value;

    SimpleExtensionState(String value) {
        this.value = value;
    }

}
