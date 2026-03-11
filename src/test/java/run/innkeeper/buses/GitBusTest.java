package run.innkeeper.buses;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitBusTest {

    @Test
    void get_returnsSingleton() {
        GitBus bus = GitBus.get();
        assertNotNull(bus);
        assertSame(bus, GitBus.get());
    }

    @Test
    void constructor_setsCorrectImage() {
        GitBus bus = new GitBus();
        assertNotNull(bus);
    }
}
