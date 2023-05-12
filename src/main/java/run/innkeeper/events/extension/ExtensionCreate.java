package run.innkeeper.events.extension;

import run.innkeeper.events.structure.ExtensionEvent;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;

public class ExtensionCreate extends ExtensionEvent {
    public ExtensionCreate(SimpleExtension event) {
        super(event);
    }
}
