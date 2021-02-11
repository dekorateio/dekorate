package io.dekorate.servicebinding.manifest;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.ManifestGenerator;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.ResourceRegistry;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.servicebinding.config.EditableServiceBindingConfig;
import io.dekorate.servicebinding.config.ServiceBindingConfig;
import io.dekorate.servicebinding.decorator.AddServiceBindingResourceDecorator;

public class ServiceBindingResourceGenerator implements ManifestGenerator<ServiceBindingConfig>  {

  private final ResourceRegistry resourceRegistry;
  private final ConfigurationRegistry configurationRegistry;

  private static final String SERVICEBINDING = "servicebinding";

  private final Logger LOGGER = LoggerFactory.getLogger();

  public ServiceBindingResourceGenerator(ResourceRegistry resources, ConfigurationRegistry configurationRegistry) {
    this.resourceRegistry = resources;
    this.configurationRegistry = configurationRegistry;
  }

  @Override
  public int order() {
    return 310;
  }

  @Override
  public String getKey() {
    return SERVICEBINDING;
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(ServiceBindingConfig.class) || type.equals(EditableServiceBindingConfig.class);
  }

  public void handle(ServiceBindingConfig config) {
    LOGGER.info("Processing service binding config.");
    addVisitors(config);
  }

  private void addVisitors(ServiceBindingConfig config) {
    resourceRegistry.decorate(new AddServiceBindingResourceDecorator(config));
  }
}
