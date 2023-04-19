package io.dekorate.certmanager.decorator;

import java.util.Optional;

import io.dekorate.certmanager.config.CA;
import io.fabric8.certmanager.api.model.v1.CAIssuerBuilder;
import io.fabric8.certmanager.api.model.v1.IssuerBuilder;
import io.fabric8.certmanager.api.model.v1.IssuerFluent;

public class AddCaIssuerResourceDecorator extends BaseAddIssuerResourceDecorator {

  private final CA config;

  public AddCaIssuerResourceDecorator(CA config, String name) {
    super(name);
    this.config = config;
  }

  @Override
  protected void visitIssuerSpec(IssuerFluent.SpecNested<IssuerBuilder> spec) {
    CAIssuerBuilder builder = new CAIssuerBuilder()
        .withSecretName(config.getSecretName());

    Optional.ofNullable(config.getCrlDistributionPoints()).ifPresent(builder::withCrlDistributionPoints);

    spec.withCa(builder.build());
  }
}
