package run.innkeeper.events.actions.guests;

import run.innkeeper.events.structure.GuestEvent;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;

public class CheckGuestDeploymentChanges extends GuestEvent {
    DeploymentSettings deploymentSetting;

    public CheckGuestDeploymentChanges(Guest guest, DeploymentSettings deploymentSetting) {
        super(guest);
        this.deploymentSetting = deploymentSetting;
    }

    public DeploymentSettings getDeploymentSetting() {
        return deploymentSetting;
    }
    public void setDeploymentSetting(DeploymentSettings deploymentSetting) {
        this.deploymentSetting = deploymentSetting;
    }
}
