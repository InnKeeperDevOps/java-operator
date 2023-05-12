package run.innkeeper.events.extension;

import run.innkeeper.events.structure.ExtensionEvent;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;

public class ExtensionDelete extends ExtensionEvent {
    public ExtensionDelete(SimpleExtension event) {
        super(event);
    }
}