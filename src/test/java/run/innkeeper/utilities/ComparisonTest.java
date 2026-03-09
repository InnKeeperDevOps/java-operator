package run.innkeeper.utilities;

import org.junit.jupiter.api.Test;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComparisonTest {

    public static class TestObj {
        private String name;
        private int age;

        public TestObj(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    @Test
    void compare_bothNull_returnsEmpty() throws Exception {
        List<String> result = Comparison.compare(null, null, "TestObj", "name");
        assertTrue(result.isEmpty());
    }

    @Test
    void compare_newObjNull_returnsAllFields() throws Exception {
        TestObj existing = new TestObj("John", 30);
        List<String> result = Comparison.compare(null, existing, "TestObj", "name", "age");
        assertEquals(2, result.size());
        assertTrue(result.contains("name"));
        assertTrue(result.contains("age"));
    }

    @Test
    void compare_existingObjNull_returnsAllFields() throws Exception {
        TestObj newObj = new TestObj("John", 30);
        List<String> result = Comparison.compare(newObj, null, "TestObj", "name", "age");
        assertEquals(2, result.size());
    }

    @Test
    void compare_differentClasses_returnsEmpty() throws Exception {
        TestObj obj1 = new TestObj("John", 30);
        String obj2 = "different";
        List<String> result = Comparison.compare(obj1, obj2, "TestObj", "name");
        assertTrue(result.isEmpty());
    }

    @Test
    void compare_sameValues_returnsEmpty() throws Exception {
        TestObj obj1 = new TestObj("John", 30);
        TestObj obj2 = new TestObj("John", 30);
        List<String> result = Comparison.compare(obj1, obj2, "TestObj", "name", "age");
        assertTrue(result.isEmpty());
    }

    @Test
    void compare_differentValues_returnsChangedFields() throws Exception {
        TestObj obj1 = new TestObj("Jane", 25);
        TestObj obj2 = new TestObj("John", 30);
        List<String> result = Comparison.compare(obj1, obj2, "TestObj", "name", "age");
        assertTrue(result.contains("TestObj.name"));
        assertTrue(result.contains("TestObj.age"));
    }

    @Test
    void compare_partialDifference_returnsOnlyChanged() throws Exception {
        TestObj obj1 = new TestObj("John", 25);
        TestObj obj2 = new TestObj("John", 30);
        List<String> result = Comparison.compare(obj1, obj2, "TestObj", "name", "age");
        assertFalse(result.contains("TestObj.name"));
        assertTrue(result.contains("TestObj.age"));
    }
}
