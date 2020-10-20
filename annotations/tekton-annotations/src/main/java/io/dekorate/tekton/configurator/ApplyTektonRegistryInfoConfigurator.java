
package io.dekorate.tekton.configurator;

import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.tekton.config.TektonConfigFluent;
import io.dekorate.utils.Strings;

public class ApplyTektonRegistryInfoConfigurator extends Configurator<TektonConfigFluent<?>> {

  private final String registry;
  private final String username;
  private final String password;
  
	public ApplyTektonRegistryInfoConfigurator(String registry, String username, String password) {
		this.registry = registry;
		this.username = username;
		this.password = password;
	}

	@Override
	public void visit(TektonConfigFluent<?> config) {
    if (Strings.isNotNullOrEmpty(registry)) {
      config.withRegistry(registry);
    }

    if (Strings.isNotNullOrEmpty(username)) {
      config.withRegistryUsername(username);
    }

    if (Strings.isNotNullOrEmpty(password)) {
      config.withRegistryPassword(password);
    }
	}
}
