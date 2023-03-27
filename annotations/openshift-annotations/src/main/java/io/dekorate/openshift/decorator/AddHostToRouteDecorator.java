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

import java.util.Arrays;
import java.util.List;

import io.dekorate.ConfigReference;
import io.dekorate.WithConfigReferences;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.openshift.api.model.RouteSpecFluent;

@Description("Add the host to the Route resource.")
public class AddHostToRouteDecorator extends NamedResourceDecorator<RouteSpecFluent<?>> implements WithConfigReferences {

  private final OpenshiftConfig config;

  public AddHostToRouteDecorator(OpenshiftConfig config) {
    super(KIND_ROUTE, config.getName());
    this.config = config;
  }

  @Override
  public void andThenVisit(RouteSpecFluent<?> spec, ObjectMeta resourceMeta) {
    if (!spec.hasHost() && config.getRoute() != null && Strings.isNotNullOrEmpty(config.getRoute().getHost())) {
      spec.withHost(config.getRoute().getHost());
    }
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddRouteDecorator.class };
  }

  @Override
  public List<ConfigReference> getConfigReferences() {
    return Arrays.asList(buildConfigReferenceHost());
  }

  private ConfigReference buildConfigReferenceHost() {
    String property = "host";
    String path = "(kind == Route && metadata.name == " + getName() + ").spec.host";
    return new ConfigReference.Builder(property, path)
        .withDescription("The host under which the application is going to be exposed.")
        .build();
  }
}
