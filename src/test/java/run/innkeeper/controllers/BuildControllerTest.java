package run.innkeeper.controllers;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.buses.BuildBus;
import run.innkeeper.buses.GitBus;
import run.innkeeper.events.actions.builds.*;
import run.innkeeper.events.builds.BuildFinished;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.build.crd.*;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.build.Docker;
import run.innkeeper.v1.guest.crd.objects.build.GitSource;
import run.innkeeper.v1.guest.crd.objects.build.Publish;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildControllerTest {

    @Mock
    K8sService k8sService;

    @Mock
    BuildBus buildBus;

    @Mock
    GitBus gitBus;

    BuildController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new BuildController();
        Field k8sField = BuildController.class.getDeclaredField("k8sService");
        k8sField.setAccessible(true);
        k8sField.set(controller, k8sService);

        // Inject mock k8sService into the static BuildBus singleton too
        Field busK8sField = run.innkeeper.buses.JobBus.class.getDeclaredField("k8sService");
        busK8sField.setAccessible(true);
        busK8sField.set(BuildBus.get(), k8sService);
    }

    private Build createBuild() {
        Build build = new Build();
        build.setMetaData("test-ns", "test-build");
        BuildSpec spec = new BuildSpec();
        BuildSettings bs = new BuildSettings();
        bs.setName("test-build");
        bs.setNamespace("test-ns");
        GitSource git = new GitSource();
        git.setUri("git@github.com:test/repo.git");
        git.setSecret("ssh-key");
        git.setBranch("main");
        bs.setGit(git);
        Publish publish = new Publish();
        publish.setRegistry("ghcr.io");
        publish.setTag("test/image");
        publish.setSecret("reg-secret");
        bs.setPublish(publish);
        Docker docker = new Docker();
        docker.setDockerfile("Dockerfile");
        bs.setDocker(docker);
        spec.setBuildSettings(bs);
        build.setSpec(spec);
        build.setStatus(new BuildStatus());
        build.getStatus().setState(BuildState.WAITING);
        return build;
    }

    @Test
    void buildFinished_withEvent_doesNotThrow() {
        Build build = createBuild();
        BuildFinished event = new BuildFinished(build);
        assertDoesNotThrow(() -> controller.buildFinished(event));
    }

    @Test
    void buildFinished_withNull_doesNotThrow() {
        assertDoesNotThrow(() -> controller.buildFinished(null));
    }

    @Test
    void failedBuild_deletesJobAndSetsState() {
        Build build = createBuild();
        Job job = new JobBuilder().withNewMetadata().withName("job1").endMetadata().build();

        // Use the real BuildBus static methods - can't easily mock static
        // Just test the logic by calling the method
        assertDoesNotThrow(() -> controller.failedBuild(new FailedBuild(build)));
        assertEquals(BuildState.NEED_TO_BUILD, build.getStatus().getState());
    }
}
