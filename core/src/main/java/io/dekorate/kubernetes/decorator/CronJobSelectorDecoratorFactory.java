
package io.dekorate.kubernetes.decorator;

import io.dekorate.SelectorDecoratorFactory;

public class CronJobSelectorDecoratorFactory implements SelectorDecoratorFactory {

  @Override
  public AddToMatchingLabelsDecorator createAddToSelectorDecorator(String name, String key, String value) {
    return new AddToMatchingLabelsDecorator(name, key, value);
  }

  @Override
  public RemoveFromMatchingLabelsDecorator createRemoveFromSelectorDecorator(String name, String key) {
    return new RemoveFromMatchingLabelsDecorator(name, key);
  }

  @Override
  public String kind() {
    return "CronJob";
  }
}
