package run.innkeeper.extensions;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionAnnotationTest {

    @Test
    void extensionAnnotation_isRuntimeRetention() {
        Retention retention = Extension.class.getAnnotation(Retention.class);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void extensionAnnotation_targetsType() {
        Target target = Extension.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertEquals(ElementType.TYPE, target.value()[0]);
    }

    @Test
    void extensionStructure_isInterface() {
        assertTrue(ExtensionStructure.class.isInterface());
    }

    @Test
    void triggerAnnotation_isRuntimeRetention() {
        Retention retention = run.innkeeper.events.structure.Trigger.class.getAnnotation(Retention.class);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void triggerAnnotation_targetsMethod() {
        Target target = run.innkeeper.events.structure.Trigger.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertEquals(ElementType.METHOD, target.value()[0]);
    }
}
