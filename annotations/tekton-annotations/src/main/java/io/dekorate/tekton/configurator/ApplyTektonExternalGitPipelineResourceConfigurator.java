
package io.dekorate.tekton.configurator;

import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.tekton.config.TektonConfigFluent;
import io.dekorate.utils.Strings;

public class ApplyTektonExternalGitPipelineResourceConfigurator extends Configurator<TektonConfigFluent<?>> {

  private final String resource;
  
	public ApplyTektonExternalGitPipelineResourceConfigurator(String resource) {
		this.resource = resource;
	}

	@Override
	public void visit(TektonConfigFluent<?> config) {
    if (Strings.isNotNullOrEmpty(resource)) {
      config.withExternalGitPipelineResource(resource);
    }
	}
}
