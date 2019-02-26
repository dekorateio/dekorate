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

import io.ap4k.kubernetes.config.Label;
import io.ap4k.kubernetes.decorator.Decorator;
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
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

public class Resources implements Coordinates {

  private static final String DEFAULT_GROUP = "kubernetes";
  private final Map<String, KubernetesListBuilder> groups = new LinkedHashMap<>();
  private final KubernetesListBuilder global = new KubernetesListBuilder();
  private final Set<Decorator> globalDecorators = new HashSet<>();

  private final Map<String, Set<Decorator>> groupDecorators = new HashMap<>();
  private final Map<String, Set<Decorator>> customDecorators = new HashMap<>();
  private final Map<String, KubernetesListBuilder> customGroups = new HashMap<>();

  // The fields below represents info that is meant to be shared across generators.
  // These are provided by annotations like: KubernetesApplication, OpenshiftApplication etc, but are used by many others.
  private final AtomicReference<String> group = new AtomicReference<>();
  private final AtomicReference<String> name = new AtomicReference<>();
  private final AtomicReference<String> version = new AtomicReference<>();
  private final Map<String, String> labels = new HashMap<String, String>();


  /**
   * Get all registered groups.
   * @return  The groups map.
   */
  public Map<String, KubernetesListBuilder> groups()  {
    return this.groups;
  }

  /**
   * Add a {@link Decorator}.
   * @param decorator   The decorator.
   */
  public void decorate(Decorator decorator) {
    globalDecorators.add(decorator);
  }

  /**
   * Add a {@link Decorator} to the specified resource group.
   * @param group     The group.
   * @param decorator   The decorator.
   */
  public void decorate(String group, Decorator decorator) {
    if (!groupDecorators.containsKey(group))  {
      groupDecorators.put(group, new TreeSet<>());
    }
    groupDecorators.get(group).add(decorator);
  }

  /**
   * Add a {@link Decorator}.
   * @param decorator   The decorator.
   */
  public void decorate(Doneable<? extends Decorator> decorator) {
    globalDecorators.add(decorator.done()); }

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
   * Add a {@link Decorator} to the specified custom group.
   * Custom groups hold custom resources and are not mixed and matched with Kubernetes/Openshift resources.
   * To add a custom decorator, you need to explicitly specify it using this method.
   * @param group       The group.
   * @param decorator   The decorator.
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
   * @param group     The group.
   * @param metadata  The resource.
   */
  public void addCustom(String group, HasMetadata metadata) {
    if (!customGroups.containsKey(group)) {
      customGroups.put(group, new KubernetesListBuilder());
    }
    customGroups.get(group).addToItems(metadata);
  }

  /**
   * Generate all resources.
   * @return A map of {@link KubernetesList} by group name.
   */
  protected Map<String, KubernetesList> generate() {
    List<HasMetadata> allGlobals = global.buildItems();
    if (groups.isEmpty()) {
      groups.put(DEFAULT_GROUP, new KubernetesListBuilder());
    }
    Map<String, KubernetesListBuilder> groups = new HashMap<>(this.groups);

    Map<String, KubernetesList> resources = new HashMap<>();
    for (Map.Entry<String, KubernetesListBuilder> entry : groups.entrySet())  {
      entry.getValue().addAllToItems(allGlobals);
    }

    groupDecorators.forEach((group, decorators) -> {
      if (groups.containsKey(group)) {
        Set<Decorator> union = new TreeSet<>();
        union.addAll(decorators);
        union.addAll(globalDecorators);
        for (Decorator d : union) {
          groups.get(group).accept(d);
        }
      }});

    groups.forEach((g, b) -> resources.put(g, b.build()));

    for (Map.Entry<String, Set<Decorator>> entry : customDecorators.entrySet()) {
      String group = entry.getKey();
      Set<Decorator> groupDecorators = entry.getValue();
      for (Decorator decorator : groupDecorators)  {
       customGroups.get(group).accept(decorator);
      }
      resources.put(group, customGroups.get(group).build());
    }
    return resources;
  }

  public String getGroup() {
    return group.get();
  }

  public void setGroup(String group) {
    this.group.compareAndSet(null, group);
  }

  public String getName() {
    return name.get();
  }

  public void setName(String name) {
    this.name.compareAndSet(null, name);
  }

  public String getVersion() {
    return version.get();
  }

  public void setVersion(String version) {
    this.version.compareAndSet(null, version);
  }

  public void addLabel(Label label) {
    this.labels.put(label.getKey(), label.getValue());
  }

  public void setLabels(Map<String, String> labels) {
    labels.entrySet().stream().map(e -> new Label(e.getKey(), e.getValue())).forEach(l -> addLabel(l));
  }

  public Map<String, String> getLabels() {
    return labels;
  }
}
