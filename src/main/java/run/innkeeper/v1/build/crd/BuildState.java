package run.innkeeper.v1.build.crd;

public enum BuildState {
    WAITING("W"),
    BUILDING("B"),
    GIT_CHECK("GC"),
    BUILD_FAILED("BF"),
    NEED_TO_BUILD("NTB");
    String value;

    BuildState(String value) {
        this.value = value;
    }
}
