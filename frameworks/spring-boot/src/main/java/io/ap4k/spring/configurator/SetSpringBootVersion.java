
package io.ap4k.spring.configurator;

import io.ap4k.kubernetes.config.ConfigKey;
import io.ap4k.kubernetes.config.ConfigurationFluent;
import io.ap4k.kubernetes.config.Configurator;

public class SetSpringBootVersion extends Configurator<ConfigurationFluent<?>> {

  public static final ConfigKey<String> RUNTIME_VERSION = new ConfigKey<>("RUNTIME_VERSION", String.class);

  @Override
  public void visit(ConfigurationFluent<?> config) {
    config.addToAttributes(RUNTIME_VERSION, getSpringBootVersion());
  }

  public static String getSpringBootVersion() {
    try {
      return Class.forName("org.springframework.boot.autoconfigure.SpringBootApplication")
        .getPackage().getImplementationVersion();
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}
