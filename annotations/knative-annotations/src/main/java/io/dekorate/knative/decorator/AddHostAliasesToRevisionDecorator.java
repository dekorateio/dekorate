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

package io.dekorate.knative.decorator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.dekorate.kubernetes.config.HostAlias;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.dekorate.utils.Strings;
import io.fabric8.knative.serving.v1.RevisionSpecFluent;
import io.fabric8.kubernetes.api.model.HostAliasBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class AddHostAliasesToRevisionDecorator extends NamedResourceDecorator<RevisionSpecFluent<?>> {

  private final HostAlias hostAlias;

  public AddHostAliasesToRevisionDecorator(HostAlias hostAlias) {
    this(ANY, hostAlias);
  }

  public AddHostAliasesToRevisionDecorator(String deploymentName, HostAlias hostAlias) {
    super(deploymentName);
    this.hostAlias = hostAlias;
  }

  public void andThenVisit(RevisionSpecFluent<?> revisionSpec, ObjectMeta resourceMeta) {
    if (Strings.isNotNullOrEmpty(hostAlias.getIp()) && Strings.isNotNullOrEmpty(hostAlias.getHostnames())) {
      List<io.fabric8.kubernetes.api.model.HostAlias> existing = revisionSpec.getHostAliases();
      if (existing == null) {
        existing = new ArrayList<>();
      }
      existing = existing.stream().filter(h -> !h.getIp().equals(hostAlias.getIp())).collect(Collectors.toList());
      List<io.fabric8.kubernetes.api.model.HostAlias> updated = new ArrayList<>(existing);
      updated.add(new HostAliasBuilder().withIp(hostAlias.getIp())
          .withHostnames(Arrays.asList(hostAlias.getHostnames().split(","))).build());
      revisionSpec.withHostAliases(updated);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AddHostAliasesToRevisionDecorator that = (AddHostAliasesToRevisionDecorator) o;
    return Objects.equals(hostAlias, that.hostAlias);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hostAlias);
  }
}
