package io.dekorate.certmanager.decorator;

import java.util.Optional;

import io.dekorate.certmanager.config.SelfSigned;
import io.fabric8.certmanager.api.model.v1.IssuerBuilder;
import io.fabric8.certmanager.api.model.v1.IssuerFluent;
import io.fabric8.certmanager.api.model.v1.SelfSignedIssuerBuilder;

public class AddSelfSignedIssuerResourceDecorator extends BaseAddIssuerResourceDecorator {

  private final SelfSigned config;

  public AddSelfSignedIssuerResourceDecorator(SelfSigned config, String name) {
    super(name);
    this.config = config;
  }

  @Override
  protected void visitIssuerSpec(IssuerFluent.SpecNested<IssuerBuilder> spec) {
    SelfSignedIssuerBuilder builder = new SelfSignedIssuerBuilder();

    Optional.ofNullable(config.getCrlDistributionPoints()).ifPresent(builder::withCrlDistributionPoints);

    spec.withSelfSigned(builder.build());
  }
}
