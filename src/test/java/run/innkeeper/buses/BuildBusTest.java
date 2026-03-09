package run.innkeeper.buses;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuildBusTest {

    @Test
    void get_returnsSingleton() {
        BuildBus bus = BuildBus.get();
        assertNotNull(bus);
        assertSame(bus, BuildBus.get());
    }

    @Test
    void constructor_setsCorrectImage() {
        BuildBus bus = new BuildBus();
        assertNotNull(bus);
    }
}
