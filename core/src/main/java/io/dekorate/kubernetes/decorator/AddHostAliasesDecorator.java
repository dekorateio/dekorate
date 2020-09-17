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
 *
**/

package io.dekorate.kubernetes.decorator;

import io.dekorate.deps.kubernetes.api.builder.Predicate;
import io.dekorate.deps.kubernetes.api.model.HostAliasBuilder;
import io.dekorate.deps.kubernetes.api.model.ObjectMeta;
import io.dekorate.deps.kubernetes.api.model.PodSpecFluent;
import io.dekorate.kubernetes.config.HostAlias;
import io.dekorate.utils.Strings;

import java.util.Arrays;
import java.util.Objects;

public class AddHostAliasesDecorator extends NamedResourceDecorator<PodSpecFluent<?>> {

 private final HostAlias hostAlias;

  public AddHostAliasesDecorator(String deploymentName, HostAlias hostAlias) {
    super(deploymentName);
    this.hostAlias = hostAlias;
  }

  public void andThenVisit(PodSpecFluent<?> podSpec, ObjectMeta resourceMeta) {
    if (Strings.isNotNullOrEmpty(hostAlias.getIp()) && Strings.isNotNullOrEmpty(hostAlias.getHostnames())) {
      Predicate<HostAliasBuilder> matchingHostAlias = host -> {
        if (host.getIp() != null)
          return host.getIp().equals(hostAlias.getIp());
        return false;
      };

      podSpec.removeMatchingFromHostAliases(matchingHostAlias);

      podSpec.addNewHostAlias()
        .withIp(hostAlias.getIp())
        .withHostnames(Arrays.asList(hostAlias.getHostnames().split(",")))
        .endHostAlias();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AddHostAliasesDecorator that = (AddHostAliasesDecorator) o;
    return Objects.equals(hostAlias, that.hostAlias);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hostAlias);
  }
}
