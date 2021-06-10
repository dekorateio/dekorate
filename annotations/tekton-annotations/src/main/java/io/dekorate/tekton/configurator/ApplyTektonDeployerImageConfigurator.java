
package io.dekorate.tekton.configurator;

import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.tekton.config.TektonConfigFluent;
import io.dekorate.utils.Strings;

public class ApplyTektonDeployerImageConfigurator extends Configurator<TektonConfigFluent<?>> {

  private final String deployerImage;

  public ApplyTektonDeployerImageConfigurator(String deployerImage) {
    this.deployerImage = deployerImage;
  }

  @Override
  public void visit(TektonConfigFluent<?> config) {
    if (Strings.isNotNullOrEmpty(deployerImage)) {
      config.withDeployerImage(deployerImage);
    }
  }
}
