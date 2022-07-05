
package io.dekorate.kubernetes.decorator;

import io.dekorate.SelectorDecoratorFactory;

public class DeploymentSelectorDecoratorFactory implements SelectorDecoratorFactory {

  @Override
  public AddToMatchingLabelsDecorator createAddToSelectorDecorator(String name, String key, String value) {
    return new AddToMatchingLabelsDecorator(name, key, value);
  }

  @Override
  public RemoveLabelFromServiceSelectorDecorator createRemoveFromSelectorDecorator(String name, String key) {
    return new RemoveLabelFromServiceSelectorDecorator(name, key);
  }

  @Override
  public String kind() {
    return "Deployment";
  }
}
