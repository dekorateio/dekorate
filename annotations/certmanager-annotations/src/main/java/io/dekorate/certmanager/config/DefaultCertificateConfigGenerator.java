package io.dekorate.certmanager.config;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.WithProject;
import io.dekorate.project.ApplyProjectInfo;

public class DefaultCertificateConfigGenerator implements CertificateConfigGenerator, WithProject {

  private final ConfigurationRegistry configurationRegistry;

  public DefaultCertificateConfigGenerator(ConfigurationRegistry configurationRegistry) {
    this.configurationRegistry = configurationRegistry;
    this.configurationRegistry.add(new ApplyProjectInfo(getProject()));
  }

  public ConfigurationRegistry getConfigurationRegistry() {
    return configurationRegistry;
  }
}
