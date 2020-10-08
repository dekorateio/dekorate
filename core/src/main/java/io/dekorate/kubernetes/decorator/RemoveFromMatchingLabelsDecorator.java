package io.dekorate.kubernetes.decorator;

import io.fabric8.kubernetes.api.model.LabelSelectorFluent;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class RemoveFromMatchingLabelsDecorator extends NamedResourceDecorator<LabelSelectorFluent<?>> {

  private String key;

  public RemoveFromMatchingLabelsDecorator(String key) {
    this(ANY, key);
  }

  public RemoveFromMatchingLabelsDecorator(String name, String key) {
    super(name);
    this.key = key;
  }

  public RemoveFromMatchingLabelsDecorator(String kind, String name, String key) {
    super(kind, name);
    this.key = key;
  }

  @Override
  public void andThenVisit(LabelSelectorFluent<?> selector, ObjectMeta resourceMeta) {
    selector.removeFromMatchLabels(key);
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ApplyLabelSelectorDecorator.class, AddToMatchingLabelsDecorator.class };
  }
}
