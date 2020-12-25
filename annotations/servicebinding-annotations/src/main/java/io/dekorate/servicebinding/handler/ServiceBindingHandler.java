package io.dekorate.servicebinding.handler;

import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Resources;
import io.dekorate.WithProject;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.servicebinding.config.EditableServiceBindingConfig;
import io.dekorate.servicebinding.config.ServiceBindingConfig;
import io.dekorate.servicebinding.decorator.AddServiceBindingResourceDecorator;

public class ServiceBindingHandler implements HandlerFactory, Handler<ServiceBindingConfig>, WithProject {

  private final Resources resources;
  private final Configurators configurators;

  private static final String SERVICEBINDING = "servicebinding";

  private final Logger LOGGER = LoggerFactory.getLogger();

  public ServiceBindingHandler() {
    this(new Resources(), new Configurators());
  }

  public ServiceBindingHandler(Resources resources, Configurators configurators) {
    this.resources = resources;
    this.configurators = configurators;
  }

  @Override
  public Handler create(Resources resources, Configurators configurators) {
    return new ServiceBindingHandler(resources, configurators);
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
    resources.decorate(new AddServiceBindingResourceDecorator(config));
  }
}
