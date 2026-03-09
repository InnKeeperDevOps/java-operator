package run.innkeeper.services;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import run.innkeeper.permission.PermissionTree;
import run.innkeeper.v1.account.crd.Account;
import run.innkeeper.v1.account.crd.AccountSpec;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    K8sService k8sService;

    @Mock
    KubernetesClient client;

    @Mock
    MixedOperation<Account, KubernetesResourceList<Account>, Resource<Account>> accountClient;

    AccountService accountService;

    @BeforeEach
    void setUp() throws Exception {
        accountService = new AccountService();
        Field k8sField = AccountService.class.getDeclaredField("k8sService");
        k8sField.setAccessible(true);
        k8sField.set(accountService, k8sService);
    }

    @Test
    void get_returnsSingleton() {
        assertNotNull(AccountService.get());
    }

    @Test
    void getAccountService_returnsSingleton() {
        assertNotNull(AccountService.getAccountService());
    }

    @Test
    void getK8sService_returnsK8sService() {
        assertEquals(k8sService, accountService.getK8sService());
    }

    @Test
    void getPermissionCache_returnsMap() {
        assertNotNull(accountService.getPermissionCache());
    }

    @Test
    void upsert_addsPermissions() {
        accountService.upsert("testuser", Arrays.asList("admin.**", "guest.read"));
        assertTrue(accountService.hasPermission("testuser", "admin.users.read"));
        assertTrue(accountService.hasPermission("testuser", "guest.read"));
    }

    @Test
    void hasPermission_returnsFalseForUnknownUser() {
        assertFalse(accountService.hasPermission("unknown", "admin.read"));
    }

    @Test
    void hasPermission_returnsFalseForMissingPerm() {
        accountService.upsert("testuser", Arrays.asList("guest.read"));
        assertFalse(accountService.hasPermission("testuser", "admin.write"));
    }

    @Test
    void delete_removesPermissions() {
        accountService.upsert("testuser", Arrays.asList("admin.**"));
        assertTrue(accountService.hasPermission("testuser", "admin.anything"));
        accountService.delete("testuser");
        assertFalse(accountService.hasPermission("testuser", "admin.anything"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void createAccount_createsAccountAndPerms() {
        DefaultOidcUser oidcUser = mock(DefaultOidcUser.class);
        when(oidcUser.getEmail()).thenReturn("test@example.com");
        when(oidcUser.getPicture()).thenReturn("https://example.com/pic.jpg");
        when(oidcUser.getFullName()).thenReturn("Test User");

        when(k8sService.getAccountClient()).thenReturn(accountClient);
        Resource<Account> resource = mock(Resource.class);
        when(accountClient.resource(any(Account.class))).thenReturn(resource);

        List<String> perms = Arrays.asList("admin.**");
        Account result = accountService.createAccount(oidcUser, perms);

        assertNotNull(result);
        verify(resource).create();
        assertTrue(accountService.hasPermission("test.example.com", "admin.anything"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAccount_withOidcUser() {
        DefaultOidcUser oidcUser = mock(DefaultOidcUser.class);
        when(oidcUser.getEmail()).thenReturn("test@example.com");

        when(k8sService.getAccountClient()).thenReturn(accountClient);
        Resource<Account> resource = mock(Resource.class);
        when(accountClient.resource(any(Account.class))).thenReturn(resource);
        Account account = new Account();
        when(resource.get()).thenReturn(account);

        Account result = accountService.getAccount(oidcUser);
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAccount_withString() {
        when(k8sService.getAccountClient()).thenReturn(accountClient);
        Resource<Account> resource = mock(Resource.class);
        when(accountClient.resource(any(Account.class))).thenReturn(resource);
        Account account = new Account();
        when(resource.get()).thenReturn(account);

        Account result = accountService.getAccount("testuser");
        assertNotNull(result);
    }

    @Test
    void getPermissionCache_withOidcUser() {
        DefaultOidcUser oidcUser = mock(DefaultOidcUser.class);
        when(oidcUser.getEmail()).thenReturn("test@example.com");

        accountService.upsert("test.example.com", Arrays.asList("admin.**"));
        PermissionTree result = accountService.getPermissionCache(oidcUser);
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAccounts_returnsList() {
        when(k8sService.getAccountClient()).thenReturn(accountClient);
        KubernetesResourceList<Account> list = mock(KubernetesResourceList.class);
        when(accountClient.list()).thenReturn(list);
        when(list.getItems()).thenReturn(Arrays.asList(new Account()));

        List<Account> result = accountService.getAccounts();
        assertEquals(1, result.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void countAccounts_returnsCount() {
        when(k8sService.getAccountClient()).thenReturn(accountClient);
        KubernetesResourceList<Account> list = mock(KubernetesResourceList.class);
        when(accountClient.list()).thenReturn(list);
        when(list.getItems()).thenReturn(Arrays.asList(new Account(), new Account()));

        assertEquals(2, accountService.countAccounts());
    }

    @SuppressWarnings("unchecked")
    @Test
    void updateUser_withPerms() {
        DefaultOidcUser oidcUser = mock(DefaultOidcUser.class);
        when(oidcUser.getEmail()).thenReturn("test@example.com");
        when(oidcUser.getPicture()).thenReturn("pic.jpg");
        when(oidcUser.getFullName()).thenReturn("Test User");

        when(k8sService.getAccountClient()).thenReturn(accountClient);
        Resource<Account> resource = mock(Resource.class);
        when(accountClient.resource(any(Account.class))).thenReturn(resource);
        Account account = new Account();
        account.setSpec(new AccountSpec());
        when(resource.get()).thenReturn(account);
        when(resource.update()).thenReturn(account);

        Account result = accountService.updateUser(oidcUser, Arrays.asList("guest.*"));
        assertNotNull(result);
        verify(resource).update();
    }

    @SuppressWarnings("unchecked")
    @Test
    void updateUser_withoutPerms() {
        DefaultOidcUser oidcUser = mock(DefaultOidcUser.class);
        when(oidcUser.getEmail()).thenReturn("test@example.com");
        when(oidcUser.getPicture()).thenReturn("pic.jpg");
        when(oidcUser.getFullName()).thenReturn("Test User");

        when(k8sService.getAccountClient()).thenReturn(accountClient);
        Resource<Account> resource = mock(Resource.class);
        when(accountClient.resource(any(Account.class))).thenReturn(resource);
        Account account = new Account();
        account.setSpec(new AccountSpec());
        when(resource.get()).thenReturn(account);
        when(resource.update()).thenReturn(account);

        Account result = accountService.updateUser(oidcUser);
        assertNotNull(result);
        verify(resource).update();
    }
}
