package run.innkeeper.v1.build;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.buses.BuildBus;
import run.innkeeper.buses.EventBus;
import run.innkeeper.buses.GitBus;
import run.innkeeper.v1.build.crd.*;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.build.GitSource;
import run.innkeeper.v1.guest.crd.objects.build.Publish;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildReconcilerTest {

    @Mock
    EventBus eventBus;

    @Mock
    BuildBus buildBus;

    @Mock
    GitBus gitBus;

    @Mock
    Context<Build> context;

    BuildReconciler reconciler;

    @BeforeEach
    void setUp() throws Exception {
        reconciler = new BuildReconciler();
        Field eventBusField = BuildReconciler.class.getDeclaredField("eventBus");
        eventBusField.setAccessible(true);
        eventBusField.set(reconciler, eventBus);
        Field buildBusField = BuildReconciler.class.getDeclaredField("buildBus");
        buildBusField.setAccessible(true);
        buildBusField.set(reconciler, buildBus);
        Field gitBusField = BuildReconciler.class.getDeclaredField("gitBus");
        gitBusField.setAccessible(true);
        gitBusField.set(reconciler, gitBus);
    }

    private Build createBuild(BuildState state) {
        Build build = new Build();
        build.setMetaData("test-ns", "test-build");
        BuildSpec spec = new BuildSpec();
        BuildSettings bs = new BuildSettings();
        bs.setName("test-build");
        bs.setNamespace("test-ns");
        GitSource git = new GitSource();
        git.setUri("git@github.com:test/repo.git");
        git.setSecret("ssh-key");
        bs.setGit(git);
        Publish publish = new Publish();
        publish.setRegistry("ghcr.io");
        publish.setTag("test/image");
        publish.setSecret("reg-secret");
        bs.setPublish(publish);
        spec.setBuildSettings(bs);
        build.setSpec(spec);
        if (state != null) {
            build.setStatus(new BuildStatus());
            build.getStatus().setState(state);
        }
        return build;
    }

    @Test
    void reconcile_nullStatus_initializesStatus() throws Exception {
        Build build = createBuild(null);

        UpdateControl<Build> result = reconciler.reconcile(build, context);

        assertNotNull(build.getStatus());
        assertEquals(BuildState.WAITING, build.getStatus().getState());
        assertTrue(result.isUpdateStatus());
    }

    @Test
    void reconcile_waitingState_firesCheckGitBuild() throws Exception {
        Build build = createBuild(BuildState.WAITING);

        reconciler.reconcile(build, context);

        verify(eventBus).fire(any());
    }

    @Test
    void reconcile_buildingState_firesMonitorBuild() throws Exception {
        Build build = createBuild(BuildState.BUILDING);

        reconciler.reconcile(build, context);

        verify(eventBus).fire(any());
    }

    @Test
    void reconcile_gitCheckState_firesMonitorGit() throws Exception {
        Build build = createBuild(BuildState.GIT_CHECK);

        reconciler.reconcile(build, context);

        verify(eventBus).fire(any());
    }

    @Test
    void cleanup_deletesBuildAndGitJobs() {
        Build build = createBuild(BuildState.BUILDING);

        DeleteControl result = reconciler.cleanup(build, context);

        verify(buildBus).delete(build.getSpec().getBuildSettings());
        verify(gitBus).delete(build.getSpec().getBuildSettings());
        assertNotNull(result);
    }

    @Test
    void cleanup_handlesExceptions() {
        Build build = createBuild(BuildState.BUILDING);
        doThrow(new RuntimeException("test")).when(buildBus).delete(any());
        doThrow(new RuntimeException("test")).when(gitBus).delete(any());

        DeleteControl result = reconciler.cleanup(build, context);
        assertNotNull(result);
    }
}
