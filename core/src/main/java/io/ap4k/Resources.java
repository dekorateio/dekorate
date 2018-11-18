package io.ap4k;

import io.fabric8.kubernetes.api.builder.Visitor;
import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Resources {

  private final Map<String, KubernetesListBuilder> groups = new LinkedHashMap<>();
  private final Set<KubernetesListBuilder> global = new LinkedHashSet<>();
  private final Set<Visitor> visitors = new HashSet<>();

  private final Map<String, KubernetesListBuilder> explicitGroups = new HashMap<>();

  /**
   * Add a {@link Visitor}.
   * @param visitor   The visitor.
   */
  public void accept(Visitor visitor) {
    visitors.add(visitor);
  }

  /**
   * Add a {@link Visitor} to the specified resource group.
   * @param group     The group.
   * @param visitor   The visitor.
   */
  public void accept(String group, Visitor visitor) {
    if (explicitGroups.containsKey(group)) {
      explicitGroups.get(group).accept(visitor);

    } else if (groups.containsKey(group)) {
      groups.get(group).accept(visitor);
    } else {
      groups.put(group, new KubernetesListBuilder().accept(visitor));
    }
  }

  /**
   * Add a {@link Visitor}.
   * @param visitor   The visitor.
   */
  public void accept(Doneable<? extends Visitor> visitor) {
    visitors.add(visitor.done());
  }

  /**
   * Add a resource to all groups.
   * @param metadata
   */
  public void add(HasMetadata metadata) {
    groups.forEach((g, b)-> b.addToItems(metadata));
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
   * Add a resource to the specified explicit group.
   * Explicit groups only accept visitors explicitly assigned to them.
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
   * Remove a resource from all groups.
   * @param metadata
   */
  public void remove(HasMetadata metadata) {
    groups.forEach((g, b)-> b.addToItems(metadata));
  }

  /**
   * Remove a resource from the specified group.
   * @param group     The group.
   * @param metadata  The resource.
   */
  public void remove(String group, HasMetadata metadata) {
    if (groups.containsKey(group)) {
      groups.get(group).removeFromItems(metadata);
    }
  }

  /**
   * Close the session an get all resource groups.
   * @return A map of {@link KubernetesList} by group name.
   */
  public Map<String, KubernetesList> generate() {
    List<HasMetadata> allGlobals = global.stream().flatMap(g -> g.buildItems().stream()).collect(Collectors.toList());

    Map<String, KubernetesList> resources = new HashMap<>();
    for (Map.Entry<String, KubernetesListBuilder> entry : groups.entrySet())  {
      entry.getValue().addAllToItems(allGlobals);
    }

    visitors.forEach(v -> groups.forEach((g, b) -> b.accept(v)));
    groups.forEach((g, b) -> resources.put(g, b.build()));
    explicitGroups.forEach((g, b) -> resources.put(g, b.build()));
    return resources;
  }
}
