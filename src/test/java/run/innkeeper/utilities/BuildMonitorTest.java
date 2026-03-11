package run.innkeeper.utilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuildMonitorTest {

    @Test
    void grabPart_findsHash() {
        String log = "hash:abc123def\nbranch:main\nauthor:testuser\ndate:2023-01-01";
        String result = BuildMonitor.grabPart(log, "hash");
        assertEquals("abc123def", result);
    }

    @Test
    void grabPart_findsBranch() {
        String log = "hash:abc123\nbranch:feature/my-branch\nauthor:user\ndate:2023-01-01";
        String result = BuildMonitor.grabPart(log, "branch");
        assertEquals("feature/my-branch", result);
    }

    @Test
    void grabPart_findsAuthor() {
        String log = "hash:abc123\nbranch:main\nauthor:testuser\ndate:2023-01-01";
        String result = BuildMonitor.grabPart(log, "author");
        assertEquals("testuser", result);
    }

    @Test
    void grabPart_findsDate() {
        String log = "hash:abc123\nbranch:main\nauthor:user\ndate:2023-01-01";
        String result = BuildMonitor.grabPart(log, "date");
        assertEquals("2023-01-01", result);
    }

    @Test
    void grabPart_returnsNullWhenNotFound() {
        String log = "some random log output";
        String result = BuildMonitor.grabPart(log, "hash");
        assertNull(result);
    }

    @Test
    void get_parsesLogCorrectly() {
        String log = "hash:abc123def\nbranch:main\nauthor:testuser\ndate:2023-01-01";
        BuildMonitor.BuildLogParts parts = BuildMonitor.get(log);

        assertEquals("abc123def", parts.getHash());
        assertEquals("main", parts.getBranch());
        assertEquals("testuser", parts.getAuthor());
        assertEquals("2023-01-01", parts.getDate());
        assertEquals(4, parts.getLines().length);
    }

    @Test
    void get_handlesLogWithNoMatchingParts() {
        String log = "just some output\nno special keys here";
        BuildMonitor.BuildLogParts parts = BuildMonitor.get(log);

        assertNull(parts.getHash());
        assertNull(parts.getBranch());
        assertNull(parts.getAuthor());
        assertNull(parts.getDate());
        assertEquals(2, parts.getLines().length);
    }

    @Test
    void buildLogParts_constructorAndGetters() {
        String[] lines = {"line1", "line2"};
        BuildMonitor.BuildLogParts parts = new BuildMonitor.BuildLogParts(lines, "hash1", "main", "author1", "2023-01-01");
        assertArrayEquals(lines, parts.getLines());
        assertEquals("hash1", parts.getHash());
        assertEquals("main", parts.getBranch());
        assertEquals("author1", parts.getAuthor());
        assertEquals("2023-01-01", parts.getDate());
    }
}
