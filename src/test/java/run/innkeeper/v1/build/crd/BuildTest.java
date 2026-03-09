package run.innkeeper.v1.build.crd;

import org.junit.jupiter.api.Test;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.build.Docker;
import run.innkeeper.v1.guest.crd.objects.build.GitSource;
import run.innkeeper.v1.guest.crd.objects.build.Publish;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class BuildTest {

    @Test
    void setMetaData_setsNameAndNamespace() {
        Build build = new Build();
        build.setMetaData("test-ns", "test-name");
        assertEquals("test-ns", build.getMetadata().getNamespace());
        assertEquals("test-name", build.getMetadata().getName());
    }

    @Test
    void buildSpec_getSetBuildSettings() {
        BuildSpec spec = new BuildSpec();
        BuildSettings bs = new BuildSettings();
        bs.setName("mybuild");
        spec.setBuildSettings(bs);
        assertEquals("mybuild", spec.getBuildSettings().getName());
    }

    @Test
    void buildStatus_getSetJobName() {
        BuildStatus status = new BuildStatus();
        status.setJobName("job-123");
        assertEquals("job-123", status.getJobName());
    }

    @Test
    void buildStatus_getSetCompleted() {
        BuildStatus status = new BuildStatus();
        BuiltContainer bc = new BuiltContainer();
        bc.setJobName("job-1");
        status.setCompleted(Arrays.asList(bc));
        assertEquals(1, status.getCompleted().size());
        assertEquals("job-1", status.getCompleted().get(0).getJobName());
    }

    @Test
    void buildStatus_getSetState() {
        BuildStatus status = new BuildStatus();
        status.setState(BuildState.BUILDING);
        assertEquals(BuildState.BUILDING, status.getState());
    }

    @Test
    void buildState_allValues() {
        assertEquals(5, BuildState.values().length);
        assertNotNull(BuildState.valueOf("WAITING"));
        assertNotNull(BuildState.valueOf("BUILDING"));
        assertNotNull(BuildState.valueOf("GIT_CHECK"));
        assertNotNull(BuildState.valueOf("BUILD_FAILED"));
        assertNotNull(BuildState.valueOf("NEED_TO_BUILD"));
    }

    @Test
    void builtContainer_allFields() {
        BuiltContainer bc = new BuiltContainer();
        bc.setJobName("job-1");
        bc.setNamespace("ns1");

        GitSource gs = new GitSource();
        gs.setUri("git@github.com:test/repo.git");
        gs.setCommit("abc123");
        gs.setBranch("main");
        gs.setSecret("ssh-secret");
        bc.setGitSource(gs);

        Publish pub = new Publish();
        pub.setRegistry("ghcr.io");
        pub.setTag("myimage");
        pub.setSecret("reg-secret");
        bc.setPublish(pub);

        Docker docker = new Docker();
        docker.setDockerfile("Dockerfile");
        docker.setWorkdir(".");
        bc.setDocker(docker);

        assertEquals("job-1", bc.getJobName());
        assertEquals("ns1", bc.getNamespace());
        assertEquals("abc123", bc.getGitSource().getCommit());
        assertEquals("main", bc.getGitSource().getBranch());
        assertEquals("ghcr.io", bc.getPublish().getRegistry());
        assertEquals("Dockerfile", bc.getDocker().getDockerfile());
        assertEquals(".", bc.getDocker().getWorkdir());
    }

    @Test
    void gitSource_copyConstructor() {
        GitSource original = new GitSource();
        original.setUri("git@github.com:test/repo.git");
        original.setSecret("my-secret");
        original.setCommit("abc123");
        original.setBranch("main");

        GitSource copy = new GitSource(original);
        assertEquals(original.getUri(), copy.getUri());
        assertEquals(original.getSecret(), copy.getSecret());
        assertEquals(original.getCommit(), copy.getCommit());
        assertEquals(original.getBranch(), copy.getBranch());
    }

    @Test
    void docker_copyConstructor() {
        Docker original = new Docker();
        original.setDockerfile("Dockerfile.custom");
        original.setWorkdir("/app");

        Docker copy = new Docker(original);
        assertEquals(original.getDockerfile(), copy.getDockerfile());
        assertEquals(original.getWorkdir(), copy.getWorkdir());
    }

    @Test
    void publish_copyConstructor() {
        Publish original = new Publish();
        original.setSecret("secret1");
        original.setRegistry("docker.io");
        original.setTag("myapp:latest");

        Publish copy = new Publish(original);
        assertEquals(original.getSecret(), copy.getSecret());
        assertEquals(original.getRegistry(), copy.getRegistry());
        assertEquals(original.getTag(), copy.getTag());
    }
}
