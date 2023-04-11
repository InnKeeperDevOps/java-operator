package run.innkeeper.events.structure;

import run.innkeeper.v1.guest.crd.Guest;

public class GuestEvent extends Event {
    Guest guest;

    public GuestEvent(Guest guest) {
        this.guest = guest;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }
}
