package run.innkeeper.events.actions.guests;

import run.innkeeper.events.structure.GuestEvent;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;

public class CheckGuestServiceChanges extends GuestEvent {
    ServiceSettings serviceSettings;

    public CheckGuestServiceChanges(Guest guest, ServiceSettings serviceSettings) {
        super(guest);
        this.serviceSettings = serviceSettings;
    }

    public ServiceSettings getServiceSettings() {
        return serviceSettings;
    }

    public void setServiceSettings(ServiceSettings serviceSettings) {
        this.serviceSettings = serviceSettings;
    }
}
