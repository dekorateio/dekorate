package io.dekorate.openshift.decorator;

import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecFluent;

public class RemoveLabelFromDeploymentSelectorDecorator extends NamedResourceDecorator<DeploymentSpecFluent<?>> {

  private String key;

  public RemoveLabelFromDeploymentSelectorDecorator(String name, String key) {
    super(name);
    this.key = key;
  }

  public RemoveLabelFromDeploymentSelectorDecorator(String kind, String name, String key) {
    super(kind, name);
    this.key = key;
  }

  @Override
  public void andThenVisit(DeploymentSpecFluent<?> spec, ObjectMeta resourceMeta) {
    spec.editOrNewSelector().removeFromMatchLabels(key).endSelector();
  }

  @Override
  public Class<? extends Decorator>[] before() {
    return new Class[] { AddLabelToDeploymentSelectorDecorator.class };
  }
}
