package io.dekorate.kind.decorator;

import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.ImageConfigurationFluent;

public class ApplyKindImageAutoloadConfiguration extends Configurator<ImageConfigurationFluent> {

  public static final String DEKORATE_KIND_AUTOLOAD = "dekorate.kind.autoload";

  @Override
  public void visit(ImageConfigurationFluent config) {
    config.withAutoLoadEnabled(Boolean.parseBoolean(System.getProperty(DEKORATE_KIND_AUTOLOAD, "true")));
  }
}
