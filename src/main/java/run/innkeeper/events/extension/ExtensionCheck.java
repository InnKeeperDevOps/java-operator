package run.innkeeper.events.extension;

import run.innkeeper.events.structure.ExtensionEvent;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;

public class ExtensionCheck extends ExtensionEvent {
    public ExtensionCheck(SimpleExtension event) {
        super(event);
    }
}
