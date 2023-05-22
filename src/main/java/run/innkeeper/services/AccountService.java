package run.innkeeper.services;

import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import run.innkeeper.permission.PermissionTree;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.account.crd.Account;
import run.innkeeper.v1.account.crd.AccountSpec;

import java.util.HashMap;
import java.util.List;

public class AccountService {
  K8sService k8sService = K8sService.get();

  public static AccountService accountService = new AccountService();
  public HashMap<String, PermissionTree> permissionCache = new HashMap<>();

  public Account createAccount(DefaultOidcUser oidcUser, List<String> perms) {
    Account account = new Account();
    account.setMetaData("innkeeper", oidcUser.getEmail().replaceAll("[^a-zA-Z0-9]", "."));
    account.setSpec(new AccountSpec());
    account.getSpec().setPermissions(perms);
    account.getSpec().setPicture(oidcUser.getPicture());
    account.getSpec().setName(oidcUser.getFullName());
    account.getSpec().setEmail(oidcUser.getEmail());
    k8sService.getAccountClient().resource(account).create();
    this.upsert(oidcUser.getEmail().replaceAll("[^a-zA-Z0-9]", "."), perms);
    return account;
  }

  public Account getAccount(DefaultOidcUser oidcUser) {
    Account account = new Account();
    account.setMetaData("innkeeper", oidcUser.getEmail().replaceAll("[^a-zA-Z0-9]", "."));
    account = k8sService.getAccountClient().resource(account).get();
    return account;
  }
  public Account getAccount(String name) {
    Account account = new Account();
    account.setMetaData("innkeeper", name);
    account = k8sService.getAccountClient().resource(account).get();
    return account;
  }

  public PermissionTree getPermissionCache(DefaultOidcUser oidcUser) {
    return permissionCache.get(oidcUser.getEmail().replaceAll("[^a-zA-Z0-9]", "."));
  }

  public List<Account> getAccounts() {
    return k8sService.getAccountClient().list().getItems();
  }

  public int countAccounts() {
    return k8sService.getAccountClient().list().getItems().size();
  }

  public Account updateUser(DefaultOidcUser oidcUser, List<String> perms) {
    Account account = new Account();
    account.setMetaData("innkeeper", oidcUser.getEmail().replaceAll("[^a-zA-Z0-9]", "."));
    account = k8sService.getAccountClient().resource(account).get();
    account.getSpec().setPermissions(perms);
    account.getSpec().setPicture(oidcUser.getPicture());
    account.getSpec().setName(oidcUser.getFullName());
    k8sService.getAccountClient().resource(account).update();
    return account;
  }

  public Account updateUser(DefaultOidcUser oidcUser) {
    Account account = new Account();
    account.setMetaData("innkeeper", oidcUser.getEmail().replaceAll("[^a-zA-Z0-9]", "."));
    account = k8sService.getAccountClient().resource(account).get();
    account.getSpec().setPicture(oidcUser.getPicture());
    account.getSpec().setName(oidcUser.getFullName());
    k8sService.getAccountClient().resource(account).update();
    return account;
  }

  public boolean hasPermission(String user, String perm) {
    PermissionTree permissionTree = permissionCache.getOrDefault(user, new PermissionTree());
    return permissionTree.hasPerm(perm);
  }

  public void upsert(String user, List<String> perms) {
    PermissionTree tree = new PermissionTree();
    permissionCache.put(user, tree);
    for (int i = 0; i < perms.size(); i++) {
      tree.add(perms.get(i));
    }
  }

  public void delete(String user) {
    permissionCache.remove(user);
  }

  public static AccountService get() {
    return accountService;
  }

  public K8sService getK8sService() {
    return k8sService;
  }

  public static AccountService getAccountService() {
    return accountService;
  }

  public HashMap<String, PermissionTree> getPermissionCache() {
    return permissionCache;
  }
}
