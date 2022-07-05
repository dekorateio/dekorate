
package io.dekorate.kubernetes.decorator;

import io.fabric8.kubernetes.api.model.LabelSelectorFluent;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class AddToMatchingLabelsDecorator extends NamedResourceDecorator<LabelSelectorFluent<?>> {

  private String key;
  private String value;

  public AddToMatchingLabelsDecorator(String name, String key, String value) {
    super(name);
    this.key = key;
    this.value = value;
  }

  public AddToMatchingLabelsDecorator(String kind, String name, String key, String value) {
    super(kind, name);
    this.key = key;
    this.value = value;
  }

  @Override
  public void andThenVisit(LabelSelectorFluent<?> selector, ObjectMeta resourceMeta) {
    selector.addToMatchLabels(key, value);
  }

  @Override
  public Class<? extends Decorator>[] before() {
    return new Class[] { RemoveFromMatchingLabelsDecorator.class };
  }
}
