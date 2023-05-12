package run.innkeeper.events.structure;

import run.innkeeper.extensions.ExtensionStructure;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;

public class ExtensionEvent extends Event {
    SimpleExtension event;

    public ExtensionEvent(SimpleExtension event) {
        this.event = event;
    }

    public SimpleExtension getEvent() {
        return event;
    }

    public void setEvent(SimpleExtension event) {
        this.event = event;
    }
}
