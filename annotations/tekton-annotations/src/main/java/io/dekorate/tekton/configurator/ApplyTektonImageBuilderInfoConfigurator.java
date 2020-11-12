
package io.dekorate.tekton.configurator;

import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.tekton.config.TektonConfigFluent;
import io.dekorate.utils.Strings;

public class ApplyTektonImageBuilderInfoConfigurator extends Configurator<TektonConfigFluent<?>> {

  private final String image;
  private final String command;
  private final String[] arguments;
  
	public ApplyTektonImageBuilderInfoConfigurator(String image, String command, String[] arguments) {
		this.image = image;
		this.command = command;
		this.arguments = arguments;
	}

	@Override
	public void visit(TektonConfigFluent<?> config) {
    if (Strings.isNotNullOrEmpty(image)) {
      config.withImageBuildImage(image);
    }

    if (Strings.isNotNullOrEmpty(command)) {
      config.withImageBuildCommand(command);
    }

    if (arguments != null) {
      config.withImageBuildArguments(arguments);
    }
	}
}
