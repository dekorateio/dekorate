package io.dekorate.servicebinding.config;

import io.dekorate.ConfigurationRegistry;

public class DefaultServiceBindingConfigGenerator implements ServiceBindingConfigGenerator {

  private final ConfigurationRegistry configurationRegistry;

  public DefaultServiceBindingConfigGenerator(ConfigurationRegistry configurationRegistry) {
    this.configurationRegistry = configurationRegistry;
  }

  public ConfigurationRegistry getConfigurationRegistry() {
    return configurationRegistry;
  }
}
