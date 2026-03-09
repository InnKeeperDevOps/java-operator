package run.innkeeper.v1.guest.crd.objects;

import org.junit.jupiter.api.Test;
import run.innkeeper.v1.guest.crd.objects.deployment.Container;
import run.innkeeper.v1.guest.crd.objects.network.TrafficType;

import static org.junit.jupiter.api.Assertions.*;

class ContainerTest {

    @Test
    void container_extendsK8sContainer() {
        Container container = new Container();
        container.setName("test-container");
        container.setBuildName("build1");

        assertEquals("test-container", container.getName());
        assertEquals("build1", container.getBuildName());
    }

    @Test
    void container_inheritsK8sContainerMethods() {
        Container container = new Container();
        container.setImage("test-image:latest");
        container.setImagePullPolicy("Always");

        assertEquals("test-image:latest", container.getImage());
        assertEquals("Always", container.getImagePullPolicy());
    }

    @Test
    void trafficType_values() {
        TrafficType[] values = TrafficType.values();
        assertTrue(values.length >= 1);
        assertNotNull(TrafficType.valueOf("ISTIO_HTTP"));
    }

    @Test
    void buildSettings_allFields() {
        BuildSettings bs = new BuildSettings();
        bs.setName("build1");
        bs.setNamespace("ns1");

        run.innkeeper.v1.guest.crd.objects.build.GitSource git = new run.innkeeper.v1.guest.crd.objects.build.GitSource();
        git.setUri("git@github.com:test/repo.git");
        git.setSecret("ssh-key");
        git.setBranch("main");
        git.setCommit("abc123");
        bs.setGit(git);

        run.innkeeper.v1.guest.crd.objects.build.Publish publish = new run.innkeeper.v1.guest.crd.objects.build.Publish();
        publish.setRegistry("ghcr.io");
        publish.setTag("test/image");
        publish.setSecret("reg-secret");
        bs.setPublish(publish);

        run.innkeeper.v1.guest.crd.objects.build.Docker docker = new run.innkeeper.v1.guest.crd.objects.build.Docker();
        docker.setDockerfile("Dockerfile");
        docker.setWorkdir(".");
        bs.setDocker(docker);

        assertEquals("build1", bs.getName());
        assertEquals("ns1", bs.getNamespace());
        assertNotNull(bs.getGit());
        assertNotNull(bs.getPublish());
        assertNotNull(bs.getDocker());
    }

    @Test
    void deploymentSettings_allFields() {
        DeploymentSettings ds = new DeploymentSettings();
        ds.setName("dep1");
        ds.setNamespace("ns1");
        ds.setReplicas(3);

        assertEquals("dep1", ds.getName());
        assertEquals("ns1", ds.getNamespace());
        assertEquals(3, ds.getReplicas());
    }

    @Test
    void serviceSettings_allFields() {
        ServiceSettings ss = new ServiceSettings();
        ss.setName("svc1");
        ss.setNamespace("ns1");
        ss.setDeployment("dep1");

        assertEquals("svc1", ss.getName());
        assertEquals("ns1", ss.getNamespace());
        assertEquals("dep1", ss.getDeployment());
        // setType only sets when current type is not null
        assertNull(ss.getType());
    }
}
