package run.innkeeper.events.actions.guests;

import run.innkeeper.events.structure.GuestEvent;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;

public class DeleteGuestBuildChanges extends GuestEvent  {
    BuildSettings buildSettings;

    public DeleteGuestBuildChanges(Guest guest, BuildSettings buildSettings) {
        super(guest);
        this.buildSettings = buildSettings;
    }

    public BuildSettings getBuildSettings() {
        return buildSettings;
    }
}
