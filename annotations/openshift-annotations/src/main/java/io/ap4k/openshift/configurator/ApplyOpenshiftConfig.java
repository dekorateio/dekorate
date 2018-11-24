package io.ap4k.openshift.configurator;

import io.ap4k.config.Configurator;
import io.ap4k.openshift.config.OpenshiftConfig;
import io.ap4k.openshift.config.SourceToImageConfigFluent;

public class ApplyOpenshiftConfig extends Configurator<SourceToImageConfigFluent> {

  private final OpenshiftConfig openshiftConfig;

  public ApplyOpenshiftConfig(OpenshiftConfig openshiftConfig) {
    this.openshiftConfig = openshiftConfig;
  }

  @Override
  public void visit(SourceToImageConfigFluent fluent) {
    fluent.withGroup(openshiftConfig.getGroup())
      .withName(openshiftConfig.getName())
      .withVersion(openshiftConfig.getVersion());

  }
}
