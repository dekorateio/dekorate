/*
 * Copyright 2020 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dekorate.kubernetes.decorator;

import io.dekorate.kubernetes.annotation.Resources;
import io.fabric8.kubernetes.api.model.ContainerFluent;
import io.fabric8.kubernetes.api.model.Quantity;

public class AddResourcesRequirementDecorator extends Decorator<ContainerFluent> {

	private static final String CPU = "cpu";
	private static final String MEMORY = "memory";

	private final Resources resources;

	public AddResourcesRequirementDecorator(Resources resources) {
		this.resources = resources;
	}

	@Override
	public void visit(ContainerFluent container) {
		container.buildResources().getRequests().put(CPU, new Quantity(resources.requests().cpu()));
		container.buildResources().getRequests().put(MEMORY, new Quantity(resources.requests().memory()));

		container.buildResources().getLimits().put(CPU, new Quantity(resources.limits().cpu()));
		container.buildResources().getLimits().put(MEMORY, new Quantity(resources.limits().memory()));
	}

	public Class<? extends Decorator>[] after() {
		return new Class[]{ResourceProvidingDecorator.class, ContainerDecorator.class, AddSidecarDecorator.class};
	}
}
