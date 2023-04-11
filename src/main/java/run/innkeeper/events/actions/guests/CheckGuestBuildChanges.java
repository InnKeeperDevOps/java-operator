package run.innkeeper.events.actions.guests;

import run.innkeeper.events.structure.GuestEvent;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;

public class CheckGuestBuildChanges extends GuestEvent {
    BuildSettings buildSettings;

    public CheckGuestBuildChanges(Guest guest, BuildSettings buildSettings) {
        super(guest);
        this.buildSettings = buildSettings;
    }

    public BuildSettings getBuild() {
        return buildSettings;
    }

    public void setBuild(BuildSettings buildSettings) {
        this.buildSettings = buildSettings;
    }
}
