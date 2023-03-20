package io.dekorate.kubernetes.decorator;

import java.util.HashMap;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecFluent;

public class AddSelectorToDeploymentSpecDecorator extends NamedResourceDecorator<DeploymentSpecFluent<?>> {

  @Override
  public void andThenVisit(DeploymentSpecFluent<?> item, ObjectMeta resourceMeta) {
    if (!item.hasSelector()) {
      item.withNewSelector()
          .withMatchLabels(new HashMap<>())
          .endSelector();
    }
  }

  @Override
  public Class<? extends Decorator>[] before() {
    return new Class[] { AddToSelectorDecorator.class };
  }
}
