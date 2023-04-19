package io.dekorate.certmanager.decorator;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import io.dekorate.certmanager.config.LocalObjectReference;
import io.dekorate.certmanager.config.Vault;
import io.fabric8.certmanager.api.model.meta.v1.SecretKeySelector;
import io.fabric8.certmanager.api.model.meta.v1.SecretKeySelectorBuilder;
import io.fabric8.certmanager.api.model.v1.IssuerBuilder;
import io.fabric8.certmanager.api.model.v1.IssuerFluent;
import io.fabric8.certmanager.api.model.v1.VaultAppRole;
import io.fabric8.certmanager.api.model.v1.VaultAuthBuilder;
import io.fabric8.certmanager.api.model.v1.VaultIssuerBuilder;
import io.fabric8.certmanager.api.model.v1.VaultKubernetesAuth;

public class AddVaultIssuerResourceDecorator extends BaseAddIssuerResourceDecorator {

  private final Vault config;

  public AddVaultIssuerResourceDecorator(Vault config, String name) {
    super(name);
    this.config = config;
  }

  @Override
  protected void visitIssuerSpec(IssuerFluent.SpecNested<IssuerBuilder> spec) {
    if (noneAuthIsSet(config.getAuthAppRole(), config.getAuthKubernetes(), config.getAuthTokenSecretRef())) {
      throw new IllegalArgumentException("No auth mechanism has been set in the Vault Issuer configuration");
    }

    if (moreThanOneAuthIsSet(config.getAuthAppRole(), config.getAuthKubernetes(), config.getAuthTokenSecretRef())) {
      throw new IllegalArgumentException("More than one auth mechanisms have been set in the Vault Issuer configuration");
    }

    VaultIssuerBuilder builder = new VaultIssuerBuilder()
        .withCaBundle(config.getCaBundle())
        .withServer(config.getServer())
        .withPath(config.getPath());

    Optional.ofNullable(config.getNamespace()).ifPresent(builder::withNamespace);

    VaultAuthBuilder authBuilder = new VaultAuthBuilder();
    Optional.ofNullable(config.getAuthTokenSecretRef()).ifPresent(r -> authBuilder.withTokenSecretRef(toSecretKeySelector(r)));
    Optional.ofNullable(config.getAuthKubernetes()).ifPresent(r -> authBuilder.withKubernetes(toVaultKubernetesAuth(r)));
    Optional.ofNullable(config.getAuthAppRole()).ifPresent(r -> authBuilder.withAppRole(toVaultAppRole(r)));
    builder.withAuth(authBuilder.build());
    spec.withVault(builder.build());
  }

  private VaultAppRole toVaultAppRole(io.dekorate.certmanager.config.VaultAppRole config) {
    VaultAppRole auth = new VaultAppRole();
    auth.setPath(config.getPath());
    auth.setRoleId(config.getRoleId());
    if (config.getSecretRef() != null) {
      auth.setSecretRef(toSecretKeySelector(config.getSecretRef()));
    }

    return auth;
  }

  private VaultKubernetesAuth toVaultKubernetesAuth(io.dekorate.certmanager.config.VaultKubernetesAuth config) {
    VaultKubernetesAuth auth = new VaultKubernetesAuth();

    auth.setMountPath(config.getMountPath());
    auth.setRole(config.getRole());
    if (config.getSecretRef() != null) {
      auth.setSecretRef(toSecretKeySelector(config.getSecretRef()));
    }

    return auth;
  }

  private SecretKeySelector toSecretKeySelector(LocalObjectReference config) {
    return new SecretKeySelectorBuilder().withName(config.getName()).withKey(config.getKey()).build();
  }

  private boolean moreThanOneAuthIsSet(Object... auths) {
    return Stream.of(auths).filter(Objects::nonNull).count() > 1;
  }

  private boolean noneAuthIsSet(Object... auths) {
    return Stream.of(auths).noneMatch(Objects::nonNull);
  }
}
