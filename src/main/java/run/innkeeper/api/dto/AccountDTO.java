package run.innkeeper.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import run.innkeeper.v1.account.crd.Account;
import run.innkeeper.v1.account.crd.AccountSpec;
import run.innkeeper.v1.account.crd.AccountStatus;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.build.crd.BuildSpec;
import run.innkeeper.v1.build.crd.BuildStatus;

public class AccountDTO{
  @JsonProperty("spec")
  AccountSpec spec;
  @JsonProperty("status")
  AccountStatus status;

  ObjectMetaDTO meta;

  public AccountDTO(Account account) {
    this.spec = account.getSpec();
    this.status = account.getStatus();
    this.meta = new ObjectMetaDTO(account.getMetadata());
  }

  public AccountSpec getSpec() {
    return spec;
  }

  public void setSpec(AccountSpec spec) {
    this.spec = spec;
  }

  public AccountStatus getStatus() {
    return status;
  }

  public void setStatus(AccountStatus status) {
    this.status = status;
  }

  public ObjectMetaDTO getMeta() {
    return meta;
  }

  public void setMeta(ObjectMetaDTO meta) {
    this.meta = meta;
  }
}
