package io.dekorate.certmanager.decorator;

import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.utils.Strings;
import io.fabric8.certmanager.api.model.v1.IssuerBuilder;
import io.fabric8.certmanager.api.model.v1.IssuerFluent;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

public abstract class BaseAddIssuerResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  protected abstract void visitIssuerSpec(IssuerFluent.SpecNested<IssuerBuilder> spec);

  private final String certificateName;

  public BaseAddIssuerResourceDecorator(String certificateName) {
    this.certificateName = certificateName;
  }

  @Override
  public void visit(KubernetesListBuilder list) {
    HasMetadata meta = getMandatoryDeploymentHasMetadata(list, ANY);
    IssuerBuilder builder = new IssuerBuilder();
    // metadata
    builder.withNewMetadata()
        .withName(getName(meta))
        .withNamespace(meta.getMetadata().getNamespace())
        .endMetadata();

    IssuerFluent.SpecNested<IssuerBuilder> spec = builder.withNewSpec();

    visitIssuerSpec(spec);

    list.addToItems(spec.endSpec().build());
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class };
  }

  @Override
  public Class<? extends Decorator>[] before() {
    return new Class[] { AddCertificateResourceDecorator.class };
  }

  private String getName(HasMetadata meta) {
    if (Strings.isNullOrEmpty(certificateName)) {
      return meta.getMetadata().getName();
    }

    return certificateName;
  }

}
