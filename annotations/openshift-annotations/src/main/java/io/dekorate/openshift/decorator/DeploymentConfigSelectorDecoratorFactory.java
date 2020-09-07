
package io.dekorate.openshift.decorator;

import io.dekorate.SelectorDecoratorFactory;

public class DeploymentConfigSelectorDecoratorFactory implements SelectorDecoratorFactory {

  @Override
  public AddLabelToDeploymentConfigSelectorDecorator createAddToSelectorDecorator(String name, String key,
      String value) {
    return new AddLabelToDeploymentConfigSelectorDecorator(name, key, value);
  }

  @Override
  public RemoveLabelFromDeploymentConfigSelectorDecorator createRemoveFromSelectorDecorator(String name, String key) {
    return new RemoveLabelFromDeploymentConfigSelectorDecorator(name, key);
  }

  @Override
  public String kind() {
    return "DeploymentConfig";
  }
}
