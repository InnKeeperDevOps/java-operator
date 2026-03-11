package run.innkeeper.utilities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HashGeneratorTest {

    @Test
    void getJobName_returnsDeterministicUUID() {
        String result1 = HashGenerator.getJobName("namespace1", "name1");
        String result2 = HashGenerator.getJobName("namespace1", "name1");
        assertEquals(result1, result2);
    }

    @Test
    void getJobName_differentInputsProduceDifferentResults() {
        String result1 = HashGenerator.getJobName("ns1", "name1");
        String result2 = HashGenerator.getJobName("ns2", "name2");
        assertNotEquals(result1, result2);
    }

    @Test
    void getJobName_returnsValidUUIDFormat() {
        String result = HashGenerator.getJobName("test-ns", "test-name");
        assertNotNull(result);
        assertTrue(result.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void getJobName_orderMatters() {
        String result1 = HashGenerator.getJobName("a", "b");
        String result2 = HashGenerator.getJobName("b", "a");
        assertNotEquals(result1, result2);
    }
}
