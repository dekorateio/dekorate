package io.ap4k.docker.configurator;

import io.ap4k.config.Configurator;
import io.ap4k.docker.config.DockerBuildConfigFluent;

public class ApplyHookConfig extends Configurator<DockerBuildConfigFluent> {

  private static final String AP4K_BUILD = "ap4k.build";

  @Override
  public void visit(DockerBuildConfigFluent config) {
    config.withAutoBuildEnabled(Boolean.parseBoolean(System.getProperty(AP4K_BUILD, String.valueOf(config.isAutoBuildEnabled()))));
  }
}
