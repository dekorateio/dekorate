
package io.dekorate.openshift.decorator;

import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecFluent;

public class AddLabelToDeploymentSelectorDecorator extends NamedResourceDecorator<DeploymentSpecFluent<?>> {

  private String key;
  private String value;

  public AddLabelToDeploymentSelectorDecorator(String name, String key, String value) {
    super(name);
    this.key = key;
    this.value = value;
  }

  public AddLabelToDeploymentSelectorDecorator(String kind, String name, String key, String value) {
    super(kind, name);
    this.key = key;
    this.value = value;
  }

  @Override
  public void andThenVisit(DeploymentSpecFluent<?> spec, ObjectMeta resourceMeta) {
    spec.editOrNewSelector()
        .addToMatchLabels(key, value)
        .endSelector();
  }

  @Override
  public Class<? extends Decorator>[] before() {
    return new Class[] { RemoveLabelFromDeploymentSelectorDecorator.class };
  }
}
