package io.dekorate.kind.configurator;

import io.dekorate.kubernetes.config.BaseConfigFluent;
import io.dekorate.kubernetes.config.Configurator;

public class ApplyKindImageAutoloadConfiguration extends Configurator<BaseConfigFluent> {

  public static final String DEKORATE_KIND_AUTOLOAD = "dekorate.kind.autoload";

  @Override
  public void visit(BaseConfigFluent config) {
    config.withAutoLoadEnabled(
        Boolean.parseBoolean(System.getProperty(DEKORATE_KIND_AUTOLOAD, String.valueOf(config.getAutoLoadEnabled()))));
  }
}
