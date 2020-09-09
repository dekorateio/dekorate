package io.dekorate.openshift.decorator;

import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.dekorate.deps.kubernetes.api.model.ObjectMeta;
import io.dekorate.deps.openshift.api.model.DeploymentConfigSpecFluent;

public class RemoveLabelFromDeploymentConfigSelectorDecorator extends NamedResourceDecorator<DeploymentConfigSpecFluent<?>> {

  private String key;

 	public RemoveLabelFromDeploymentConfigSelectorDecorator(String name, String key) {
		super(name);
		this.key = key;
	}

	public RemoveLabelFromDeploymentConfigSelectorDecorator(String kind, String name, String key) {
		super(kind, name);
		this.key = key;
	}   

	@Override
	public void andThenVisit(DeploymentConfigSpecFluent<?> spec, ObjectMeta resourceMeta) {
    spec.removeFromSelector(key);
	}

	@Override
	public Class<? extends Decorator>[] before() {
		return new Class[] { AddLabelToDeploymentConfigSelectorDecorator.class };
	}
}
