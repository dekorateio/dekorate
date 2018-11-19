package io.ap4k.openshift.visitor;

import io.ap4k.openshift.config.OpenshiftConfig;
import io.ap4k.openshift.config.SourceToImageConfigFluent;
import io.fabric8.kubernetes.api.builder.Visitor;

public class ApplyOpenshiftConfig implements Visitor<SourceToImageConfigFluent> {

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
