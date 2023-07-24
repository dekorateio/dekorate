package io.dekorate.certmanager.decorator;

import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.fabric8.certmanager.api.model.v1.IssuerBuilder;
import io.fabric8.certmanager.api.model.v1.IssuerFluent;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

public abstract class BaseAddIssuerResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  protected abstract void visitIssuerSpec(IssuerFluent<?>.SpecNested<IssuerBuilder> spec);

  private final String name;

  public BaseAddIssuerResourceDecorator(String name) {
    this.name = name;
  }

  @Override
  public void visit(KubernetesListBuilder list) {
    IssuerBuilder builder = new IssuerBuilder();
    // metadata
    builder.withNewMetadata()
        .withName(name)
        .endMetadata();

    IssuerFluent<?>.SpecNested<IssuerBuilder> spec = builder.withNewSpec();

    visitIssuerSpec(spec);

    list.addToItems(spec.endSpec().build());
  }

  @Override
  public Class<? extends Decorator>[] before() {
    return new Class[] { AddCertificateResourceDecorator.class };
  }
}
