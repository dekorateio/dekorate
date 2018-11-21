package io.ap4k.openshift.visitor;

import io.ap4k.deps.kubernetes.api.builder.Visitor;
import io.ap4k.openshift.config.SourceToImageConfigFluent;

public class ApplyHookConfig  implements Visitor<SourceToImageConfigFluent> {

  private static final String AP4K_DEPLOY = "ap4k.deploy";

  @Override
  public void visit(SourceToImageConfigFluent config) {
    config.withAutoDeployEnabled(Boolean.parseBoolean(System.getProperty(AP4K_DEPLOY, String.valueOf(config.isAutoDeployEnabled()))));
  }
}
