package io.dekorate.kubernetes.decorator;

import io.dekorate.deps.kubernetes.api.model.ObjectMeta;
import io.dekorate.deps.kubernetes.api.model.ServiceSpecFluent;

public class RemoveLabelFromServiceSelectorDecorator extends NamedResourceDecorator<ServiceSpecFluent<?>> {

  private String key;

 	public RemoveLabelFromServiceSelectorDecorator(String name, String key) {
		super(name);
		this.key = key;
	}

	public RemoveLabelFromServiceSelectorDecorator(String kind, String name, String key) {
		super(kind, name);
		this.key = key;
	}   

	@Override
	public void andThenVisit(ServiceSpecFluent<?> spec, ObjectMeta resourceMeta) {
    spec.removeFromSelector(key);
	}

	@Override
	public Class<? extends Decorator>[] after() {
		return new Class[] { AddLabelToServiceSelectorDecorator.class };
	}
  
}
