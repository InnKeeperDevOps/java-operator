package run.innkeeper.v1.simpleExtensions.crd;

public class SimpleExtensionStatus {
    SimpleExtensionState currentState = SimpleExtensionState.NEED_TO_CREATE;

    public SimpleExtensionState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(SimpleExtensionState currentState) {
        this.currentState = currentState;
    }
}
