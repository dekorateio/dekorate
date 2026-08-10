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
package io.dekorate;

import static io.dekorate.utils.Development.isVerbose;
import static io.dekorate.utils.TopologicalSort.sortDecorators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.utils.Metadata;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

public class ResourceRegistry {

  private static final String DEFAULT_GROUP = "kubernetes";
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRegistry.class);
  private final Map<String, KubernetesListBuilder> groups = new LinkedHashMap<>();
  private final KubernetesListBuilder common = new KubernetesListBuilder();
  private final Set<Decorator> globalDecorators = new HashSet<>();

  private final Map<String, Set<Decorator>> groupDecorators = new HashMap<>();
  private final Map<String, Set<Decorator>> customDecorators = new HashMap<>();
  private final Map<String, KubernetesListBuilder> customGroups = new HashMap<>();

  /**
   * Get all registered groups.
   *
   * @return The groups map.
   */
  public Map<String, KubernetesListBuilder> groups() {
    return this.groups;
  }

  /**
   * Get the global builder
   *
   * @return The groups map.
   */
  public KubernetesListBuilder common() {
    return this.common;
  }

  /**
   * Add a {@link Decorator}.
   *
   * @param decorator The decorator.
   */
  public void decorate(Decorator decorator) {
    globalDecorators.add(decorator);
  }

  /**
   * Add a {@link Decorator} to the specified resource group.
   *
   * @param group The group.
   * @param decorator The decorator.
   */
  public void decorate(String group, Decorator decorator) {
    if (!groupDecorators.containsKey(group)) {
      groupDecorators.put(group, new TreeSet<>());
    }
    groupDecorators.get(group).add(decorator);
  }

  /**
   * Add a resource to all groups.
   *
   * @param metadata
   */
  public void add(HasMetadata metadata) {
    common.addToItems(metadata);
  }

  /**
   * Add a resource to the specified group.
   *
   * @param group The group.
   * @param metadata The resource.
   */
  public void add(String group, HasMetadata metadata) {
    if (!groups.containsKey(group)) {
      groups.put(group, new KubernetesListBuilder());
    }

    if (!groups.get(group).hasMatchingItem(Metadata.matching(metadata))) {
      groups.get(group).addToItems(metadata);
    }
  }

  /**
   * Add a {@link Decorator} to the specified custom group.
   * Custom groups hold custom resources and are not mixed and matched with Kubernetes/Openshift resources.
   * To add a custom decorator, you need to explicitly specify it using this method.
   *
   * @param group The group.
   * @param decorator The decorator.
   */
  public void decorateCustom(String group, Decorator decorator) {
    if (!customDecorators.containsKey(group)) {
      customDecorators.put(group, new TreeSet<>());
    }
    customDecorators.get(group).add(decorator);
  }

  /**
   * Add a resource to the specified custom group.
   * Custom groups hold custom resources and are not mixed and matched with Kubernetes/Openshift resources.
   * To add a custom resource, you need to explicitly specify it using this method.
   *
   * @param group The group.
   * @param metadata The resource.
   */
  public void addCustom(String group, HasMetadata metadata) {
    if (!customGroups.containsKey(group)) {
      customGroups.put(group, new KubernetesListBuilder());
    }
    if (!customGroups.get(group).hasMatchingItem(Metadata.matching(metadata))) {
      customGroups.get(group).addToItems(metadata);
    }
  }

  /**
   * Generate all resources.
   *
   * @return A map of {@link KubernetesList} by group name.
   */
  protected Map<String, KubernetesList> generate() {
    if (!this.common.buildItems().isEmpty()) {
      if (this.groups.isEmpty()) {
        KubernetesListBuilder builder = new KubernetesListBuilder();
        builder.addToItems(common.buildItems().toArray(new HasMetadata[common.buildItems().size()]));
        this.groups.put(DEFAULT_GROUP, builder);
      } else {
        this.groups.forEach((group, builder) -> builder
            .addToItems(common.buildItems().toArray(new HasMetadata[common.buildItems().size()])));
      }
    }

    Map<String, KubernetesListBuilder> groups = new HashMap<>(this.groups);

    Map<String, KubernetesList> resources = new HashMap<>();

    groups.forEach((group, l) -> {
      if (!groupDecorators.containsKey(group) || groupDecorators.get(group).isEmpty()) {
        groupDecorators.put(group, globalDecorators);
      }
    });

    groupDecorators.forEach((group, decorators) -> {
      if (groups.containsKey(group)) {
        log("Handling group '%s'", group);
        Set<Decorator> union = new TreeSet<>();
        union.addAll(decorators);
        union.addAll(globalDecorators);
        for (Decorator d : sortDecorators(union)) {
          log("Applying decorator '%s'", d.getClass().getName());
          groups.get(group).accept(d);
        }
      }
    });
    groups.forEach((g, b) -> resources.put(g, b.build()));

    if (customDecorators.isEmpty()) {
      customGroups.forEach((group, l) -> customDecorators.put(group, globalDecorators));
    }

    customDecorators.forEach((group, decorators) -> {
      if (customGroups.containsKey(group)) {
        log("Handling group '%s'", group);
        Set<Decorator> union = new TreeSet<>();
        union.addAll(decorators);
        union.addAll(globalDecorators);
        for (Decorator d : sortDecorators(union)) {
          log("Applying decorator '%s'", d.getClass().getName());
          customGroups.get(group).accept(d);
        }
      }
    });
    customGroups.forEach((g, b) -> resources.put(g, b.build()));

    return resources;
  }

  /**
   * @deprecated since 3.5.3. Use `getConfigReferences(group)` instead.
   */
  @Deprecated
  public List<WithConfigReferences> getConfigReferences() {
    Set<Decorator> allDecorators = new HashSet<>();
    allDecorators.addAll(globalDecorators);
    groupDecorators.values().forEach(allDecorators::addAll);
    customDecorators.values().forEach(allDecorators::addAll);

    return extractConfigReferences(allDecorators);
  }

  public List<WithConfigReferences> getConfigReferences(String group) {
    Set<Decorator> allDecorators = new HashSet<>();
    allDecorators.addAll(globalDecorators);
    Optional.ofNullable(groupDecorators.get(group)).ifPresent(allDecorators::addAll);
    Optional.ofNullable(customDecorators.get(group)).ifPresent(allDecorators::addAll);

    return extractConfigReferences(allDecorators);
  }

  private List<WithConfigReferences> extractConfigReferences(Set<Decorator> allDecorators) {
    Set<Decorator> configReferences = new HashSet<>();
    for (Decorator decorator : allDecorators) {
      if (decorator instanceof WithConfigReferences) {
        configReferences.add(decorator);
      }
    }

    return sortDecorators(configReferences)
        .stream()
        .map(WithConfigReferences.class::cast)
        .collect(Collectors.toList());
  }

  private void log(String format, Object... args) {
    if (isVerbose()) {
      LOGGER.info(format, args);
    }
  }
}
