
package io.dekorate.kubernetes.decorator;

import io.dekorate.SelectorDecoratorFactory;

public class ServiceSelectorDecoratorFactory implements SelectorDecoratorFactory {

  @Override
  public AddLabelToServiceSelectorDecorator createAddToSelectorDecorator(String name, String key, String value) {
    return new AddLabelToServiceSelectorDecorator(name, key, value);
  }

  @Override
  public RemoveLabelFromServiceSelectorDecorator createRemoveFromSelectorDecorator(String name, String key) {
    return new RemoveLabelFromServiceSelectorDecorator(name, key);
  }

  @Override
  public String kind() {
    return "Service";
  }
}
