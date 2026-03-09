package run.innkeeper.v1.account;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.innkeeper.services.AccountService;
import run.innkeeper.v1.account.crd.Account;
import run.innkeeper.v1.account.crd.AccountSpec;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountReconcilerTest {

    @Mock
    AccountService accountService;

    @Mock
    Context<Account> context;

    AccountReconciler reconciler;

    @BeforeEach
    void setUp() throws Exception {
        reconciler = new AccountReconciler();
        Field field = AccountReconciler.class.getDeclaredField("accountService");
        field.setAccessible(true);
        field.set(reconciler, accountService);
    }

    @Test
    void reconcile_upsertsAccount() {
        Account account = new Account();
        account.setMetaData("innkeeper", "testuser");
        AccountSpec spec = new AccountSpec();
        spec.setPermissions(Arrays.asList("admin.**"));
        account.setSpec(spec);

        UpdateControl<Account> result = reconciler.reconcile(account, context);

        verify(accountService).upsert("testuser", Arrays.asList("admin.**"));
        assertFalse(result.isUpdateStatus());
    }

    @Test
    void cleanup_deletesAccount() {
        Account account = new Account();
        account.setMetaData("innkeeper", "testuser");
        AccountSpec spec = new AccountSpec();
        spec.setEmail("test@example.com");
        account.setSpec(spec);

        DeleteControl result = reconciler.cleanup(account, context);

        verify(accountService).delete("test@example.com");
        assertNotNull(result);
    }
}
