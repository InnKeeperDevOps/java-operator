package run.innkeeper.events.actions.guests;

import run.innkeeper.events.structure.GuestEvent;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionSpec;

public class CheckGuestExtensionChanges extends GuestEvent {
    SimpleExtensionSpec simpleExtensionSpec;

    public CheckGuestExtensionChanges(Guest guest, SimpleExtensionSpec simpleExtensionSpec) {
        super(guest);
        this.simpleExtensionSpec = simpleExtensionSpec;
    }

    public SimpleExtensionSpec getSimpleExtensionSpec() {
        return simpleExtensionSpec;
    }

    public void setSimpleExtensionSpec(SimpleExtensionSpec simpleExtensionSpec) {
        this.simpleExtensionSpec = simpleExtensionSpec;
    }
}
