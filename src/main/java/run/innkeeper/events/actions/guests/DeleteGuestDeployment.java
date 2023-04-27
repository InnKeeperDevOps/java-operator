package run.innkeeper.events.actions.guests;

import run.innkeeper.events.structure.GuestEvent;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;

public class DeleteGuestDeploymentChanges extends GuestEvent {
    DeploymentSettings deploymentSettings;

    public DeleteGuestDeploymentChanges(Guest guest, DeploymentSettings deploymentSettings) {
        super(guest);
        this.deploymentSettings = deploymentSettings;
    }

    public DeploymentSettings getDeploymentSettings() {
        return deploymentSettings;
    }
}
