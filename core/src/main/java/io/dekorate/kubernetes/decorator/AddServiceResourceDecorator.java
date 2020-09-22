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

package io.dekorate.kubernetes.decorator;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.utils.Labels;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;

@Description("Add a service to the list.")
public class AddServiceResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private static final Logger LOGGER = LoggerFactory.getLogger();

  private final BaseConfig config;

  public AddServiceResourceDecorator(BaseConfig config) {
    this.config = config;
  }

  public void visit(KubernetesListBuilder list) {
    if (contains(list, "v1", "Service", config.getName())) {
      return;
    }

    list.addNewServiceItem()
        .withNewMetadata()
        .withName(config.getName())
        .withLabels(Labels.createLabels(config))
        .endMetadata()
        .withNewSpec()
        .withType(config.getServiceType().name())
        .withSelector(Labels.createLabels(config))
        .withPorts(Arrays.asList(config.getPorts()).stream()
            .filter(distinct(p -> p.getName())).map(this::toServicePort).collect(Collectors.toList()))
        .endSpec()
        .endServiceItem();
  }

  private ServicePort toServicePort(Port port) {
    return new ServicePortBuilder()
        .withName(port.getName())
        .withPort(port.getContainerPort())
        .withTargetPort(new IntOrString(port.getHostPort() > 0 ? port.getHostPort() : port.getContainerPort()))
        .build();
  }

  public static <T> Predicate<T> distinct(Function<? super T, Object> keyExtractor) {
    Map<Object, Boolean> map = new ConcurrentHashMap<>();
    return t -> {
      Object key = keyExtractor.apply(t);
      if (key == null) {
        LOGGER.warning("Found incomplete port definition (name is missing). The port will be ignored.");
        return false;
      } else {
        return map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
      }
    };
  }
}
