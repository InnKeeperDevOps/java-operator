package run.innkeeper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void main_classExists() {
        assertNotNull(Main.class);
    }

    @Test
    void main_hasK8sServiceField() throws Exception {
        var field = Main.class.getDeclaredField("k8sService");
        assertNotNull(field);
    }

    @Test
    void main_hasEventBusField() throws Exception {
        var field = Main.class.getDeclaredField("eventBus");
        assertNotNull(field);
    }
}
