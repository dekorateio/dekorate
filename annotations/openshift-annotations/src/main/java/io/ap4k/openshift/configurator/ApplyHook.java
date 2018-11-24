package io.ap4k.openshift.configurator;

import io.ap4k.config.Configurator;
import io.ap4k.openshift.config.SourceToImageConfigFluent;

public class ApplyHook extends Configurator<SourceToImageConfigFluent> {

  private static final String AP4K_DEPLOY = "ap4k.deploy";

  @Override
  public void visit(SourceToImageConfigFluent config) {
    config.withAutoDeployEnabled(Boolean.parseBoolean(System.getProperty(AP4K_DEPLOY, String.valueOf(config.isAutoDeployEnabled()))));
  }
}
