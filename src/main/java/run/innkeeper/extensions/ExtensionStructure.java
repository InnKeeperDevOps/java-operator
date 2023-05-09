package run.innkeeper.events.structure;

import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionState;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionStatus;

public interface ExtensionStructure {
    SimpleExtensionState create(SimpleExtension simpleExtension);
    SimpleExtensionState update(SimpleExtension simpleExtension);

    void delete(SimpleExtension simpleExtension);
}
