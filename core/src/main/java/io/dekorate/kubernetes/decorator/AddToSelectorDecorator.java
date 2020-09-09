/**
 * Copyright (C) 2020 Original Authors
 *     
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
**/

package io.dekorate.kubernetes.decorator;

import java.util.Optional;

import io.dekorate.SelectorDecoratorFactories;
import io.dekorate.SelectorDecoratorFactory;
import io.dekorate.deps.kubernetes.api.builder.VisitableBuilder;
import io.dekorate.deps.kubernetes.api.model.ObjectMeta;

public class AddToSelectorDecorator extends NamedResourceDecorator<VisitableBuilder> {

  private final String key;
  private final String value;

	public AddToSelectorDecorator(String key, String value) {
		super(ANY);
		this.key = key;
		this.value = value;
	}

	public AddToSelectorDecorator(String name, String key, String value) {
		super(name);
		this.key = key;
		this.value = value;
	}

	public AddToSelectorDecorator(String kind, String name, String key, String value) {
		super(kind, name);
		this.key = key;
		this.value = value;
	}


	@Override
	public void andThenVisit(VisitableBuilder builder ,String kind, ObjectMeta resourceMeta) {
    Optional<SelectorDecoratorFactory> factory = SelectorDecoratorFactories.find(kind);
    factory.ifPresent(f -> f.createAddToSelectorDecorator(resourceMeta.getName(), key, value));
	}

	@Override
	public void andThenVisit(VisitableBuilder item, ObjectMeta resourceMeta) {
    //Not needed
	}
}
