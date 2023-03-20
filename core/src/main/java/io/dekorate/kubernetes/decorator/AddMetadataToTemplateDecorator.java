package io.dekorate.kubernetes.decorator;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodTemplateSpecFluent;

public class AddMetadataToTemplateDecorator extends NamedResourceDecorator<PodTemplateSpecFluent<?>> {

  @Override
  public void andThenVisit(PodTemplateSpecFluent<?> item, ObjectMeta resourceMeta) {
    if (!item.hasMetadata()) {
      item.withNewMetadata().endMetadata();
    }
  }

  @Override
  public Class<? extends Decorator>[] before() {
    return new Class[] { AddLabelDecorator.class, RemoveLabelDecorator.class };
  }
}
