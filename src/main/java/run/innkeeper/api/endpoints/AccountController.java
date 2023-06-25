package run.innkeeper.api.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.annotations.UserAuthorized;
import run.innkeeper.api.dto.AccountDTO;
import run.innkeeper.services.AccountService;
import run.innkeeper.services.K8sService;
import run.innkeeper.v1.account.crd.Account;
import run.innkeeper.v1.account.crd.AccountSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/oauth/account", "/token/account"})
public class AccountController{
  AccountService accountService = AccountService.get();
  K8sService k8sService = K8sService.get();

  @GetMapping("/")
  @UserAuthorized("user.list")
  public List<AccountDTO> getAccounts() {
    return accountService.getAccounts().stream().map(a -> new AccountDTO(a)).collect(Collectors.toList());
  }

  @GetMapping("/{name}/")
  @UserAuthorized("user.get")
  public AccountDTO getAccount(@PathVariable String name) {
    return new AccountDTO(accountService.getAccount(name));
  }

  @PutMapping("/{name}/")
  @UserAuthorized("user.edit")
  public AccountDTO editAccount(@PathVariable String name, @RequestBody AccountSpec accountSpec) {
    Account account = accountService.getAccount(name);
    if (account != null) {
      account.setSpec(accountSpec);
      k8sService.getAccountClient().resource(account).update();
    }
    return new AccountDTO(account);
  }

  @PutMapping("/{name}/grant/{permission}")
  @UserAuthorized("user.permission.grant")
  public AccountDTO addPermission(@PathVariable String name, @PathVariable String permission) {
    Account account = accountService.getAccount(name);
    if (account != null) {
      if(account.getSpec().getPermissions()==null) {
        account.getSpec().setPermissions(new ArrayList<>());
      }
      account.getSpec().getPermissions().add(permission);
      k8sService.getAccountClient().resource(account).update();
    }
    return new AccountDTO(account);
  }

  @PutMapping("/{name}/revoke/{permission}")
  @UserAuthorized("user.permission.revoke")
  public AccountDTO removePermission(@PathVariable String name, @PathVariable String permission) {
    Account account = accountService.getAccount(name);
    if (account != null) {
      if(account.getSpec().getPermissions()!=null) {
        account.getSpec().setPermissions(account.getSpec().getPermissions().stream().filter(p -> !p.equals(permission)).collect(Collectors.toList()));
      }
      k8sService.getAccountClient().resource(account).update();
    }
    return new AccountDTO(account);
  }
}
