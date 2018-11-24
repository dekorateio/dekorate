package io.ap4k.spring.configurator;

import io.ap4k.config.ConfigKey;
import io.ap4k.config.Configurator;
import io.ap4k.config.KubernetesConfigBuilder;
import io.ap4k.deps.kubernetes.api.builder.TypedVisitor;

public class SetSpringBootRuntime extends Configurator<KubernetesConfigBuilder> {

  // TODO : Make this property generic as it will also be ued by Vert.x, Tornthail, ...
  public static final ConfigKey<String> RUNTIME_TYPE = new ConfigKey<>("RUNTIME_TYPE", String.class);
  private static String RUNTIME_SPRING_BOOT = "spring-boot";

  @Override
  public void visit(KubernetesConfigBuilder config) {
    config.addToAttributes(RUNTIME_TYPE,RUNTIME_SPRING_BOOT);
  }
}
