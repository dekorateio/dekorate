/**
 * Copyright 2018 The original authors.
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

package io.dekorate.openshift.decorator;

import static io.dekorate.openshift.decorator.AddRouteDecorator.KIND_ROUTE;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.openshift.api.model.RouteSpecFluent;

@Description("Add the service to the Route resource.")
public class AddServiceToRouteDecorator extends NamedResourceDecorator<RouteSpecFluent<?>> {

  private final OpenshiftConfig config;

  public AddServiceToRouteDecorator(OpenshiftConfig config) {
    super(KIND_ROUTE, config.getName());
    this.config = config;
  }

  @Override
  public void andThenVisit(RouteSpecFluent<?> spec, ObjectMeta resourceMeta) {
    if (!spec.hasTo()) {
      spec.withNewTo()
          .withKind("Service")
          .withName(config.getName())
          .endTo();
    }
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddRouteDecorator.class };
  }
}
