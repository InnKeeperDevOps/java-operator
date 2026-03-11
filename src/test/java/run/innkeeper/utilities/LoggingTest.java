package run.innkeeper.utilities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoggingTest {

    @Test
    void getStack_returnsStackTraceElement() {
        StackTraceElement element = Logging.getStack();
        assertNotNull(element);
    }

    @Test
    void debug_stringMessage_doesNotThrow() {
        assertDoesNotThrow(() -> Logging.debug("test debug message"));
    }

    @Test
    void error_stringMessage_doesNotThrow() {
        assertDoesNotThrow(() -> Logging.error("test error message"));
    }

    @Test
    void info_stringMessage_doesNotThrow() {
        assertDoesNotThrow(() -> Logging.info("test info message"));
    }

    @Test
    void info_object_doesNotThrow() {
        assertDoesNotThrow(() -> Logging.info(new Object() {
            public String name = "test";
        }));
    }

    @Test
    void debug_object_doesNotThrow() {
        assertDoesNotThrow(() -> Logging.debug(new Object() {
            public String name = "test";
        }));
    }

    @Test
    void error_object_doesNotThrow() {
        assertDoesNotThrow(() -> Logging.error(new Object() {
            public String name = "test";
        }));
    }

    @Test
    void info_nonSerializableObject_handlesGracefully() {
        // Object that cannot be serialized to JSON
        Object unserializable = new Object() {
            public Object self = this; // circular reference
        };
        assertDoesNotThrow(() -> Logging.info(unserializable));
    }

    @Test
    void debug_nonSerializableObject_handlesGracefully() {
        Object unserializable = new Object() {
            public Object self = this;
        };
        assertDoesNotThrow(() -> Logging.debug(unserializable));
    }

    @Test
    void error_nonSerializableObject_handlesGracefully() {
        Object unserializable = new Object() {
            public Object self = this;
        };
        assertDoesNotThrow(() -> Logging.error(unserializable));
    }
}
