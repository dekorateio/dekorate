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

import static io.dekorate.utils.Ports.getPortByFilter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.dekorate.ConfigReference;
import io.dekorate.WithConfigReferences;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.openshift.api.model.RouteSpecFluent;

@Description("Add the port to the Route resource.")
public class AddPortToRouteDecorator extends NamedResourceDecorator<RouteSpecFluent<?>> implements WithConfigReferences {

  private final OpenshiftConfig config;

  public AddPortToRouteDecorator(OpenshiftConfig config) {
    this.config = config;
  }

  @Override
  public void andThenVisit(RouteSpecFluent<?> spec, ObjectMeta resourceMeta) {
    String targetPortName = config.getRoute().getTargetPort();
    Optional<Port> port = getPortByFilter(p -> Strings.equals(p.getName(), targetPortName), config);
    if (port.isPresent() && !spec.hasPath()) {
      spec.withPath(port.get().getPath());
    }

    if (Strings.isNotNullOrEmpty(targetPortName) && !spec.hasPort()) {
      spec.editOrNewPort().withNewTargetPort(targetPortName).endPort();
    }
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddRouteDecorator.class };
  }

  @Override
  public List<ConfigReference> getConfigReferences() {
    return Arrays.asList(buildConfigReferencePath());
  }

  private ConfigReference buildConfigReferencePath() {
    String property = "path";
    String path = "(kind == Route && metadata.name == " + getName() + ").spec.path";
    return new ConfigReference(property, path);
  }
}
