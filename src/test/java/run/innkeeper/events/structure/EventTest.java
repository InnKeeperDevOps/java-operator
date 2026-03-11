package run.innkeeper.events.structure;

import org.junit.jupiter.api.Test;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.deployment.crd.Deployment;
import run.innkeeper.v1.guest.crd.Guest;
import run.innkeeper.v1.service.crd.Service;
import run.innkeeper.v1.simpleExtensions.crd.SimpleExtension;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void event_canBeInstantiated() {
        Event event = new Event() {};
        assertNotNull(event);
    }

    @Test
    void buildEvent_wrapsBuilds() {
        Build build = new Build();
        build.setMetaData("ns", "build1");
        BuildEvent event = new BuildEvent(build);
        assertEquals(build, event.getBuild());
    }

    @Test
    void buildEvent_settersWork() {
        Build build1 = new Build();
        build1.setMetaData("ns", "build1");
        Build build2 = new Build();
        build2.setMetaData("ns", "build2");
        BuildEvent event = new BuildEvent(build1);
        event.setBuild(build2);
        assertEquals(build2, event.getBuild());
    }

    @Test
    void deploymentEvent_wrapsDeployment() {
        Deployment dep = new Deployment();
        dep.setMetaData("ns", "dep1");
        DeploymentEvent event = new DeploymentEvent(dep);
        assertEquals(dep, event.getDeployment());
    }

    @Test
    void deploymentEvent_settersWork() {
        Deployment dep = new Deployment();
        dep.setMetaData("ns", "dep1");
        DeploymentEvent event = new DeploymentEvent(dep);
        Deployment dep2 = new Deployment();
        event.setDeployment(dep2);
        assertEquals(dep2, event.getDeployment());
    }

    @Test
    void serviceEvent_wrapsService() {
        Service svc = new Service();
        svc.setMetaData("ns", "svc1");
        ServiceEvent event = new ServiceEvent(svc);
        assertEquals(svc, event.getService());
    }

    @Test
    void serviceEvent_settersWork() {
        Service svc = new Service();
        ServiceEvent event = new ServiceEvent(svc);
        Service svc2 = new Service();
        event.setService(svc2);
        assertEquals(svc2, event.getService());
    }

    @Test
    void guestEvent_wrapsGuest() {
        Guest guest = new Guest();
        guest.setMetaData("ns", "guest1");
        GuestEvent event = new GuestEvent(guest);
        assertEquals(guest, event.getGuest());
    }

    @Test
    void guestEvent_settersWork() {
        Guest guest = new Guest();
        GuestEvent event = new GuestEvent(guest);
        Guest guest2 = new Guest();
        event.setGuest(guest2);
        assertEquals(guest2, event.getGuest());
    }

    @Test
    void extensionEvent_wrapsExtension() {
        SimpleExtension se = new SimpleExtension();
        se.setMetaData("ns", "ext1");
        ExtensionEvent event = new ExtensionEvent(se);
        assertEquals(se, event.getEvent());
    }

    @Test
    void extensionEvent_settersWork() {
        SimpleExtension se = new SimpleExtension();
        ExtensionEvent event = new ExtensionEvent(se);
        SimpleExtension se2 = new SimpleExtension();
        event.setEvent(se2);
        assertEquals(se2, event.getEvent());
    }

    @Test
    void buildWithContainer_wrapsBuildAndContainer() {
        Build build = new Build();
        run.innkeeper.v1.guest.crd.objects.deployment.Container container =
            new run.innkeeper.v1.guest.crd.objects.deployment.Container();
        container.setName("c1");
        container.setBuildName("build1");
        BuildWithContainer bwc = new BuildWithContainer(build, container);
        assertEquals(build, bwc.getBuild());
        assertEquals(container, bwc.getContainer());
    }

    @Test
    void buildWithContainer_gettersWork() {
        Build build = new Build();
        run.innkeeper.v1.guest.crd.objects.deployment.Container container =
            new run.innkeeper.v1.guest.crd.objects.deployment.Container();
        BuildWithContainer bwc = new BuildWithContainer(build, container);
        assertSame(build, bwc.getBuild());
        assertSame(container, bwc.getContainer());
    }
}
