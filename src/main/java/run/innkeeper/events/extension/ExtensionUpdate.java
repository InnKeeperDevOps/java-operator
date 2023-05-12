package run.innkeeper.events.extension;

import run.innkeeper.events.structure.Event;
import run.innkeeper.events.structure.ExtensionEvent;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;

public class ExtensionUpdate extends ExtensionEvent {
    public ExtensionUpdate(SimpleExtension event) {
        super(event);
    }
}
