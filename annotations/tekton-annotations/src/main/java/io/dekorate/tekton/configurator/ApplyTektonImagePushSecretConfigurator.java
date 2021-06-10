
package io.dekorate.tekton.configurator;

import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.tekton.config.TektonConfigFluent;
import io.dekorate.utils.Strings;

public class ApplyTektonImagePushSecretConfigurator extends Configurator<TektonConfigFluent<?>> {

  private final String secret;

  public ApplyTektonImagePushSecretConfigurator(String secret) {
    this.secret = secret;
  }

  @Override
  public void visit(TektonConfigFluent<?> config) {
    if (Strings.isNotNullOrEmpty(secret)) {
      config.withImagePushSecret(secret);
    }
  }
}
