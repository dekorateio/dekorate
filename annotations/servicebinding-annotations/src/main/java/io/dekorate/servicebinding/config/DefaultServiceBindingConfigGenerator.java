package io.dekorate.servicebinding.config;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.WithProject;
import io.dekorate.project.ApplyProjectInfo;

public class DefaultServiceBindingConfigGenerator implements ServiceBindingConfigGenerator, WithProject {

  private final ConfigurationRegistry configurationRegistry;

  public DefaultServiceBindingConfigGenerator(ConfigurationRegistry configurationRegistry) {
    this.configurationRegistry = configurationRegistry;
    this.configurationRegistry.add(new ApplyProjectInfo(getProject()));
  }

  public ConfigurationRegistry getConfigurationRegistry() {
    return configurationRegistry;
  }
}
