package run.innkeeper.v1.account.crd;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void setMetaData_setsNameAndNamespace() {
        Account account = new Account();
        account.setMetaData("test-ns", "test-name");
        assertEquals("test-ns", account.getMetadata().getNamespace());
        assertEquals("test-name", account.getMetadata().getName());
    }

    @Test
    void accountSpec_permissions() {
        AccountSpec spec = new AccountSpec();
        spec.setPermissions(Arrays.asList("admin.**", "guest.*"));
        assertEquals(2, spec.getPermissions().size());
        assertEquals("admin.**", spec.getPermissions().get(0));
    }

    @Test
    void accountSpec_roles() {
        AccountSpec spec = new AccountSpec();
        spec.setRoles(Arrays.asList("admin", "user"));
        assertEquals(2, spec.getRoles().size());
    }

    @Test
    void accountSpec_email() {
        AccountSpec spec = new AccountSpec();
        spec.setEmail("test@example.com");
        assertEquals("test@example.com", spec.getEmail());
    }

    @Test
    void accountSpec_picture() {
        AccountSpec spec = new AccountSpec();
        spec.setPicture("https://example.com/pic.jpg");
        assertEquals("https://example.com/pic.jpg", spec.getPicture());
    }

    @Test
    void accountSpec_name() {
        AccountSpec spec = new AccountSpec();
        spec.setName("John Doe");
        assertEquals("John Doe", spec.getName());
    }

    @Test
    void accountStatus_instantiation() {
        AccountStatus status = new AccountStatus();
        assertNotNull(status);
    }
}
