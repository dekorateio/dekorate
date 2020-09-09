
package io.dekorate.kubernetes.decorator;

import io.dekorate.deps.kubernetes.api.model.ObjectMeta;
import io.dekorate.deps.kubernetes.api.model.ServiceSpecFluent;

public class AddLabelToServiceSelectorDecorator extends NamedResourceDecorator<ServiceSpecFluent<?>> {

  private String key;
  private String value;

 	public AddLabelToServiceSelectorDecorator(String name, String key, String value) {
		super(name);
		this.key = key;
		this.value = value;
	}

	public AddLabelToServiceSelectorDecorator(String kind, String name, String key, String value) {
		super(kind, name);
		this.key = key;
		this.value = value;
	}   

	@Override
	public void andThenVisit(ServiceSpecFluent<?> spec, ObjectMeta resourceMeta) {
    spec.addToSelector(key, value);
	}

	@Override
	public Class<? extends Decorator>[] before() {
		return new Class[] { RemoveLabelFromServiceSelectorDecorator.class };
	}
 
}
