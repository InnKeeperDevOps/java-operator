package run.innkeeper.buses;

import org.junit.jupiter.api.Test;
import run.innkeeper.events.server.ServerStarted;
import run.innkeeper.events.structure.Event;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {

    @Test
    void get_returnsSingleton() {
        EventBus bus = EventBus.get();
        assertNotNull(bus);
        assertSame(bus, EventBus.get());
    }

    @Test
    void register_doesNotThrow() {
        EventBus bus = new EventBus();
        assertDoesNotThrow(() -> bus.register());
    }

    @Test
    void fire_unknownEvent_doesNotThrow() {
        EventBus bus = new EventBus();
        bus.register();
        assertDoesNotThrow(() -> bus.fire(new Event() {}));
    }

    @Test
    void fire_serverStartedWithoutRegistration_doesNotThrow() {
        EventBus bus = new EventBus();
        assertDoesNotThrow(() -> bus.fire(new ServerStarted()));
    }
}
