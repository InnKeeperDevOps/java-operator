package run.innkeeper.buses;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.guest.crd.objects.BuildSettings;
import run.innkeeper.v1.guest.crd.objects.build.Docker;
import run.innkeeper.v1.guest.crd.objects.build.GitSource;
import run.innkeeper.v1.guest.crd.objects.build.Publish;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobBusTest {

    @Mock
    K8sService k8sService;

    JobBus jobBus;

    @BeforeEach
    void setUp() throws Exception {
        jobBus = new JobBus("test", "test-image:latest");
        Field k8sField = JobBus.class.getDeclaredField("k8sService");
        k8sField.setAccessible(true);
        k8sField.set(jobBus, k8sService);
    }

    private BuildSettings createBuildSettings() {
        BuildSettings bs = new BuildSettings();
        bs.setName("my-build");
        bs.setNamespace("test-ns");

        GitSource git = new GitSource();
        git.setUri("git@github.com:test/repo.git");
        git.setSecret("ssh-key");
        git.setBranch("main");
        git.setCommit("abc123");
        bs.setGit(git);

        Publish publish = new Publish();
        publish.setRegistry("ghcr.io");
        publish.setTag("test/image");
        publish.setSecret("reg-secret");
        bs.setPublish(publish);

        Docker docker = new Docker();
        docker.setDockerfile("Dockerfile.custom");
        docker.setWorkdir("/app");
        bs.setDocker(docker);

        return bs;
    }

    @Test
    void create_createsJobWithCorrectSpec() {
        BuildSettings bs = createBuildSettings();
        Job result = jobBus.create(bs);

        assertNotNull(result);
        assertNotNull(result.getMetadata().getName());
        assertEquals("test-ns", result.getMetadata().getNamespace());
        verify(k8sService).createJob(result);
    }

    @Test
    void delete_callsDeleteJob() {
        BuildSettings bs = createBuildSettings();
        jobBus.delete(bs);
        verify(k8sService).deleteJob(any(Job.class));
    }

    @Test
    void get_withBuildSettings_callsGetJob() {
        BuildSettings bs = createBuildSettings();
        when(k8sService.getJob(any(Job.class))).thenReturn(null);
        Job result = jobBus.get(bs);
        assertNull(result);
    }

    @Test
    void get_withJob_callsGetJob() {
        Job job = new Job();
        when(k8sService.getJob(job)).thenReturn(job);
        Job result = jobBus.get(job);
        assertEquals(job, result);
    }

    @Test
    void newJob_hasCorrectMetadata() {
        BuildSettings bs = createBuildSettings();
        Job job = jobBus.newJob(bs);
        assertNotNull(job.getMetadata().getName());
        assertTrue(job.getMetadata().getName().startsWith("test-"));
        assertEquals("test-ns", job.getMetadata().getNamespace());
    }

    @Test
    void getJobContainer_setsAllFields() {
        BuildSettings bs = createBuildSettings();
        Container container = jobBus.getJobContainer(bs);

        assertNotNull(container);
        assertEquals("test-image:latest", container.getImage());
        assertNotNull(container.getEnv());
        assertNotNull(container.getSecurityContext());
        assertTrue(container.getSecurityContext().getPrivileged());
        assertTrue(container.getSecurityContext().getAllowPrivilegeEscalation());
        assertNotNull(container.getVolumeMounts());
    }

    @Test
    void podTemplateSpec_setsRestartPolicyNever() {
        BuildSettings bs = createBuildSettings();
        PodTemplateSpec spec = jobBus.podTemplateSpec(bs);

        assertNotNull(spec);
        assertEquals("Never", spec.getSpec().getRestartPolicy());
        assertEquals(1, spec.getSpec().getContainers().size());
        assertEquals(1, spec.getSpec().getVolumes().size());
    }

    @Test
    void sshVolumeMount_setsCorrectPath() {
        BuildSettings bs = createBuildSettings();
        var mount = jobBus.sshVolumeMount(bs);

        assertEquals("ssh-key", mount.getName());
        assertEquals("/ssh/", mount.getMountPath());
        assertTrue(mount.getReadOnly());
    }

    @Test
    void create_withoutDockerSettings_usesDefaults() {
        BuildSettings bs = createBuildSettings();
        bs.setDocker(null);

        Job result = jobBus.create(bs);
        assertNotNull(result);
    }

    @Test
    void create_withNullBranch_skipsEnvVar() {
        BuildSettings bs = createBuildSettings();
        bs.getGit().setBranch(null);
        bs.getGit().setCommit(null);
        bs.setDocker(null);

        Job result = jobBus.create(bs);
        assertNotNull(result);
    }

    @Test
    void create_withDockerNullDockerfile() {
        BuildSettings bs = createBuildSettings();
        bs.getDocker().setDockerfile(null);
        bs.getDocker().setWorkdir(null);

        Job result = jobBus.create(bs);
        assertNotNull(result);
    }
}
