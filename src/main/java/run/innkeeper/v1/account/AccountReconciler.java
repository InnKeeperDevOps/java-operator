package run.innkeeper.v1.account;

import io.javaoperatorsdk.operator.api.reconciler.Cleaner;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import run.innkeeper.services.AccountService;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.account.crd.Account;
import run.innkeeper.v1.build.crd.Build;

@ControllerConfiguration()
public class AccountReconciler  implements Reconciler<Account>, Cleaner<Account>{
  AccountService accountService = AccountService.get();
  @Override
  public DeleteControl cleanup(Account resource, Context<Account> context) {
    accountService.delete(resource.getSpec().getEmail());
    return DeleteControl.defaultDelete();
  }

  @Override
  public UpdateControl<Account> reconcile(Account resource, Context<Account> context) {
    accountService.upsert(resource.getMetadata().getName(), resource.getSpec().getPermissions());
    return UpdateControl.noUpdate();
  }
}
