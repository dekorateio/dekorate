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
package io.ap4k;

import io.ap4k.decorator.Decorator;
import io.ap4k.deps.kubernetes.api.builder.Visitor;
import io.ap4k.deps.kubernetes.api.model.Doneable;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.KubernetesListBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Resources {

  private final Map<String, KubernetesListBuilder> groups = new LinkedHashMap<>();
  private final KubernetesListBuilder global = new KubernetesListBuilder();
  private final Set<Decorator> decorators = new HashSet<>();

  private final Map<String, Decorator> explicitDecorators = new HashMap<>();
  private final Map<String, KubernetesListBuilder> explicitGroups = new HashMap<>();

  /**
   * Add a {@link Visitor}.
   * @param decorator   The decorator.
   */
  public void accept(Decorator decorator) {
    decorators.add(decorator);
  }

  /**
   * Add a {@link Visitor} to the specified resource group.
   * @param group     The group.
   * @param decorator   The decorator.
   */
  public void accept(String group, Decorator decorator) {
    if (groups.containsKey(group)) {
      groups.get(group).accept(decorator);
    } else {
      groups.put(group, new KubernetesListBuilder().accept(decorator));
    }
  }

  /**
   * Add a {@link Visitor}.
   * @param decorator   The decorator.
   */
  public void accept(Doneable<? extends Decorator> decorator) {
    decorators.add(decorator.done());
  }

  /**
   * Add a resource to all groups.
   * @param metadata
   */
  public void add(HasMetadata metadata) {
    global.addToItems(metadata);
  }

  /**
   * Add a resource to the specified group.
   * @param group     The group.
   * @param metadata  The resource.
   */
  public void add(String group, HasMetadata metadata) {
    if (!groups.containsKey(group)) {
      groups.put(group, new KubernetesListBuilder());
    }
    groups.get(group).addToItems(metadata);
  }

  /**
   * Add a {@link Visitor} to the specified resource explicit group.
   * Explicit groups are groups that only get resources and decorators that have been explicitly specified.
   * @param group     The group.
   * @param decorator   The decorator.
   */
  public void acceptExplicit(String group, Decorator decorator) {
    explicitDecorators.put(group, decorator);
  }
  /**
   * Add a resource to the specified explicit group.
   * Explicit groups only accept decorators explicitly assigned to them.
   * @param group     The group.
   * @param metadata  The resource.
   */
  public void addExplicit(String group, HasMetadata metadata) {
    if (!explicitGroups.containsKey(group)) {
      explicitGroups.put(group, new KubernetesListBuilder());
    }
    explicitGroups.get(group).addToItems(metadata);
  }

  /**
   * Close the session an get all resource groups.
   * @return A map of {@link KubernetesList} by group name.
   */
  public Map<String, KubernetesList> generate() {
    List<HasMetadata> allGlobals = global.buildItems();
    Map<String, KubernetesListBuilder> groups = new HashMap<>(this.groups);

    Map<String, KubernetesList> resources = new HashMap<>();
    for (Map.Entry<String, KubernetesListBuilder> entry : groups.entrySet())  {
      entry.getValue().addAllToItems(allGlobals);
    }

    decorators.forEach(v -> groups.forEach((g, b) -> b.accept(v)));
    groups.forEach((g, b) -> resources.put(g, b.build()));

    explicitDecorators.forEach((g, d) -> explicitGroups.get(g).accept(d));
    explicitGroups.forEach((g, b) -> resources.put(g, b.build()));
    return resources;
  }
}
