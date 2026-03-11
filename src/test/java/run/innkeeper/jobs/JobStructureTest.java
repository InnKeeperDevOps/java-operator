package run.innkeeper.jobs;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.services.K8sService;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobStructureTest {

    @Mock
    K8sService k8sService;

    JobStructure jobStructure;

    @BeforeEach
    void setUp() throws Exception {
        Map<String, String> envVars = new LinkedHashMap<>();
        envVars.put("KEY1", "value1");
        envVars.put("KEY2", "value2");
        jobStructure = new JobStructure("test-job", "test-image:latest", envVars);
        Field k8sField = JobStructure.class.getDeclaredField("k8sService");
        k8sField.setAccessible(true);
        k8sField.set(jobStructure, k8sService);
    }

    @Test
    void getName() {
        assertEquals("test-job", jobStructure.getName());
    }

    @Test
    void setName() {
        jobStructure.setName("new-name");
        assertEquals("new-name", jobStructure.getName());
    }

    @Test
    void getImage() {
        assertEquals("test-image:latest", jobStructure.getImage());
    }

    @Test
    void setImage() {
        jobStructure.setImage("new-image:v2");
        assertEquals("new-image:v2", jobStructure.getImage());
    }

    @Test
    void getEnVars() {
        assertEquals(2, jobStructure.getEnVars().size());
    }

    @Test
    void setEnVars() {
        Map<String, String> newVars = new LinkedHashMap<>();
        newVars.put("NEW_KEY", "new_value");
        jobStructure.setEnVars(newVars);
        assertEquals(1, jobStructure.getEnVars().size());
    }

    @Test
    void newJob_buildsCorrectJob() {
        Job job = jobStructure.newJob();
        assertNotNull(job);
        assertEquals("test-job", job.getMetadata().getName());
        assertEquals("innkeeper", job.getMetadata().getNamespace());
        assertEquals("Never", job.getSpec().getTemplate().getSpec().getRestartPolicy());
        assertEquals("test-image:latest", job.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
        assertEquals(2, job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().size());
    }

    @Test
    void create_callsK8sService() {
        jobStructure.create();
        verify(k8sService).createJob(any(Job.class));
    }

    @Test
    void delete_callsK8sService() {
        jobStructure.delete();
        verify(k8sService).deleteJob(any(Job.class));
    }

    @Test
    void get_callsK8sService() {
        Job job = new Job();
        when(k8sService.getJob(any(Job.class))).thenReturn(job);
        Job result = jobStructure.get();
        assertNotNull(result);
    }

    @Test
    void logs_callsK8sService() {
        when(k8sService.logs(any(Job.class))).thenReturn("log output");
        String result = jobStructure.logs();
        assertEquals("log output", result);
    }

    @Test
    void logStream_callsK8sService() {
        when(k8sService.logStream(any(Job.class))).thenReturn(null);
        assertNull(jobStructure.logStream());
    }

    @Test
    void logWatch_callsK8sService() {
        when(k8sService.logWatch(any(Job.class))).thenReturn(null);
        assertNull(jobStructure.logWatch());
    }
}
