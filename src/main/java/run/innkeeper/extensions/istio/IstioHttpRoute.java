package run.innkeeper.extensions;

import run.innkeeper.events.structure.Extension;
import run.innkeeper.events.structure.ExtensionStructure;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionState;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionStatus;

@Extension("HTTP_ISTIO")
public class IstioHttpRoute implements ExtensionStructure {

    @Override
    public SimpleExtensionState create(SimpleExtension simpleExtension) {
        return SimpleExtensionState.NEED_TO_CREATE;
    }

    @Override
    public SimpleExtensionState update(SimpleExtension simpleExtension) {
        return SimpleExtensionState.NEED_TO_UPDATE;
    }

    @Override
    public void delete(SimpleExtension simpleExtension) {

    }
}
