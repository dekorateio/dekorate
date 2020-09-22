package io.dekorate.halkyon.handler;

import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Resources;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.BaseConfigBuilder;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.EditableBaseConfig;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;

// this is used only to provide a fallback BaseConfig when none exists
public class ComponentBaseConfigHandler implements Handler<BaseConfig>, HandlerFactory, WithProject {

  @Override
  public int order() {
    return 200;
  }

  @Override
  public String getKey() {
    return ResourceGroup.NAME;
  }

  @Override
  public Handler create(Resources resources, Configurators configurators) {
    return new ComponentBaseConfigHandler();
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(BaseConfig.class) ||
        type.equals(EditableBaseConfig.class);
  }

  @Override
  public void handle(BaseConfig config) {

  }

  @Override
  public ConfigurationSupplier<BaseConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<>(new BaseConfigBuilder().accept(new ApplyProjectInfo(p)));
  }
}
