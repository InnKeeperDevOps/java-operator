package run.innkeeper.events;

import org.junit.jupiter.api.Test;
import run.innkeeper.events.actions.builds.*;
import run.innkeeper.events.actions.deployments.*;
import run.innkeeper.events.actions.guests.*;
import run.innkeeper.events.actions.services.*;
import run.innkeeper.events.builds.*;
import run.innkeeper.events.extension.*;
import run.innkeeper.events.server.ServerStarted;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.DeploymentSettings;
import run.innkeeper.v1.guest.crd.objects.ServiceSettings;
import run.innkeeper.v1.service.crd.Service;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtensionSpec;

import static org.junit.jupiter.api.Assertions.*;

class EventSubclassTest {

    @Test
    void buildStarted() {
        Build build = new Build();
        BuildStarted event = new BuildStarted(build);
        assertEquals(build, event.getBuild());
    }

    @Test
    void buildFinished() {
        Build build = new Build();
        BuildFinished event = new BuildFinished(build);
        assertEquals(build, event.getBuild());
    }

    @Test
    void buildCheck() {
        Build build = new Build();
        BuildCheck event = new BuildCheck(build);
        assertEquals(build, event.getBuild());
    }

    @Test
    void startBuild() {
        Build build = new Build();
        StartBuild event = new StartBuild(build);
        assertEquals(build, event.getBuild());
    }

    @Test
    void monitorBuild() {
        Build build = new Build();
        MonitorBuild event = new MonitorBuild(build);
        assertEquals(build, event.getBuild());
    }

    @Test
    void checkGitBuild() {
        Build build = new Build();
        CheckGitBuild event = new CheckGitBuild(build);
        assertEquals(build, event.getBuild());
    }

    @Test
    void monitorGit() {
        Build build = new Build();
        MonitorGit event = new MonitorGit(build);
        assertEquals(build, event.getBuild());
    }

    @Test
    void failedBuild() {
        Build build = new Build();
        FailedBuild event = new FailedBuild(build);
        assertEquals(build, event.getBuild());
    }

    @Test
    void updateBuild() {
        Build build = new Build();
        BuildSettings newBs = new BuildSettings();
        newBs.setName("new-build");
        UpdateBuild event = new UpdateBuild(build, newBs);
        assertEquals(build, event.getBuild());
        assertEquals(newBs, event.getNewBuild());
    }

    @Test
    void createDeployment() {
        Deployment dep = new Deployment();
        CreateDeployment event = new CreateDeployment(dep);
        assertEquals(dep, event.getDeployment());
    }

    @Test
    void updateDeployment() {
        Deployment dep = new Deployment();
        UpdateDeployment event = new UpdateDeployment(dep);
        assertEquals(dep, event.getDeployment());
    }

    @Test
    void checkDeployment() {
        Deployment dep = new Deployment();
        CheckDeployment event = new CheckDeployment(dep);
        assertEquals(dep, event.getDeployment());
    }

    @Test
    void createService() {
        Service svc = new Service();
        CreateService event = new CreateService(svc);
        assertEquals(svc, event.getService());
    }

    @Test
    void updateService() {
        Service svc = new Service();
        UpdateService event = new UpdateService(svc);
        assertEquals(svc, event.getService());
    }

    @Test
    void checkGuestBuildChanges() {
        Guest guest = new Guest();
        BuildSettings bs = new BuildSettings();
        bs.setName("build1");
        CheckGuestBuildChanges event = new CheckGuestBuildChanges(guest, bs);
        assertEquals(guest, event.getGuest());
        assertEquals(bs, event.getBuild());
        BuildSettings bs2 = new BuildSettings();
        event.setBuild(bs2);
        assertEquals(bs2, event.getBuild());
    }

    @Test
    void checkGuestDeploymentChanges() {
        Guest guest = new Guest();
        DeploymentSettings ds = new DeploymentSettings();
        CheckGuestDeploymentChanges event = new CheckGuestDeploymentChanges(guest, ds);
        assertEquals(guest, event.getGuest());
        assertEquals(ds, event.getDeploymentSetting());
        DeploymentSettings ds2 = new DeploymentSettings();
        event.setDeploymentSetting(ds2);
        assertEquals(ds2, event.getDeploymentSetting());
    }

    @Test
    void checkGuestServiceChanges() {
        Guest guest = new Guest();
        ServiceSettings ss = new ServiceSettings();
        CheckGuestServiceChanges event = new CheckGuestServiceChanges(guest, ss);
        assertEquals(guest, event.getGuest());
        assertEquals(ss, event.getServiceSettings());
        ServiceSettings ss2 = new ServiceSettings();
        event.setServiceSettings(ss2);
        assertEquals(ss2, event.getServiceSettings());
    }

    @Test
    void checkGuestExtensionChanges() {
        Guest guest = new Guest();
        SimpleExtensionSpec ses = new SimpleExtensionSpec();
        CheckGuestExtensionChanges event = new CheckGuestExtensionChanges(guest, ses);
        assertEquals(guest, event.getGuest());
        assertEquals(ses, event.getSimpleExtensionSpec());
        SimpleExtensionSpec ses2 = new SimpleExtensionSpec();
        event.setSimpleExtensionSpec(ses2);
        assertEquals(ses2, event.getSimpleExtensionSpec());
    }

    @Test
    void deleteGuestBuild() {
        Guest guest = new Guest();
        BuildSettings bs = new BuildSettings();
        DeleteGuestBuild event = new DeleteGuestBuild(guest, bs);
        assertEquals(guest, event.getGuest());
        assertEquals(bs, event.getBuildSettings());
    }

    @Test
    void deleteGuestDeployment() {
        Guest guest = new Guest();
        DeploymentSettings ds = new DeploymentSettings();
        DeleteGuestDeployment event = new DeleteGuestDeployment(guest, ds);
        assertEquals(guest, event.getGuest());
        assertEquals(ds, event.getDeploymentSettings());
    }

    @Test
    void deleteGuestService() {
        Guest guest = new Guest();
        ServiceSettings ss = new ServiceSettings();
        DeleteGuestService event = new DeleteGuestService(guest, ss);
        assertEquals(guest, event.getGuest());
        assertEquals(ss, event.getServiceSettings());
        ServiceSettings ss2 = new ServiceSettings();
        event.setServiceSettings(ss2);
        assertEquals(ss2, event.getServiceSettings());
    }

    @Test
    void deleteGuestExtension() {
        Guest guest = new Guest();
        SimpleExtensionSpec ses = new SimpleExtensionSpec();
        DeleteGuestExtension event = new DeleteGuestExtension(guest, ses);
        assertEquals(guest, event.getGuest());
        assertEquals(ses, event.getSimpleExtensionSpec());
        SimpleExtensionSpec ses2 = new SimpleExtensionSpec();
        event.setSimpleExtensionSpec(ses2);
        assertEquals(ses2, event.getSimpleExtensionSpec());
    }

    @Test
    void extensionCreate() {
        SimpleExtension se = new SimpleExtension();
        ExtensionCreate event = new ExtensionCreate(se);
        assertEquals(se, event.getEvent());
    }

    @Test
    void extensionUpdate() {
        SimpleExtension se = new SimpleExtension();
        ExtensionUpdate event = new ExtensionUpdate(se);
        assertEquals(se, event.getEvent());
    }

    @Test
    void extensionDelete() {
        SimpleExtension se = new SimpleExtension();
        ExtensionDelete event = new ExtensionDelete(se);
        assertEquals(se, event.getEvent());
    }

    @Test
    void extensionCheck() {
        SimpleExtension se = new SimpleExtension();
        ExtensionCheck event = new ExtensionCheck(se);
        assertEquals(se, event.getEvent());
    }

    @Test
    void serverStarted() {
        ServerStarted event = new ServerStarted();
        assertNotNull(event);
    }
}
