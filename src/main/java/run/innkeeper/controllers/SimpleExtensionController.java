package run.innkeeper.controllers;

import run.innkeeper.buses.ExtensionBus;
import run.innkeeper.events.extension.ExtensionCheck;
import run.innkeeper.events.extension.ExtensionCreate;
import run.innkeeper.events.extension.ExtensionDelete;
import run.innkeeper.events.extension.ExtensionUpdate;
import run.innkeeper.events.structure.Trigger;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionState;

public class SimpleExtensionController {
    ExtensionBus extensionBus = ExtensionBus.getExtensionBus();

    @Trigger(ExtensionUpdate.class)
    public void update(ExtensionUpdate event){
        SimpleExtensionState simpleExtensionState = extensionBus.update(event.getEvent());
        if(simpleExtensionState!=null){
            event.getEvent().getStatus().setCurrentState(simpleExtensionState);
        }
    }
    @Trigger(ExtensionCreate.class)
    public void create(ExtensionCreate event){
        SimpleExtensionState simpleExtensionState = extensionBus.create(event.getEvent());
        if(simpleExtensionState!=null){
            event.getEvent().getStatus().setCurrentState(simpleExtensionState);
        }
    }
    @Trigger(ExtensionCheck.class)
    public void check(ExtensionCheck event){
        SimpleExtensionState simpleExtensionState = extensionBus.check(event.getEvent());
        if(simpleExtensionState!=null){
            event.getEvent().getStatus().setCurrentState(simpleExtensionState);
        }
    }
    @Trigger(ExtensionDelete.class)
    public void delete(ExtensionDelete event){
        extensionBus.delete(event.getEvent());
    }
}
