package io.dekorate.utils;

import static io.dekorate.utils.Development.isVerbose;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.kubernetes.decorator.Decorator;

public final class TopologicalSort {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopologicalSort.class);

  private TopologicalSort() {

  }

  /**
   * Adapt the order driven by before and after dependencies into a direct direction `dependsOn`.
   * 
   * @param items
   * @return
   */
  public static List<Decorator> sortDecorators(Collection<Decorator> items) {
    // This dictionary is used to verify a decorator is in place and register the hierarchy.
    Map<Class, List<Decorator>> dictionary = createDictionary(items);
    Map<Object, Node<Decorator>> unordered = new HashMap<>();
    for (Decorator item : items) {
      Class identity = item.getClass();
      Class[] after = item.after();
      Class[] before = item.before();
      Node<Decorator> node = unordered.get(identity);
      if (node == null) {
        node = new Node<>(identity);
        unordered.put(identity, node);
      }

      node.references.add(item);
      if (after != null) {
        // the after order is the same as the dependsOn order, so we simply add it.
        for (Class a : after) {
          List<Decorator> afterDecorators = dictionary.get(a);
          if (afterDecorators != null) {
            for (Decorator afterDecorator : afterDecorators) {
              if (!afterDecorator.getClass().equals(identity)) {
                node.getDepends().add(afterDecorator.getClass());
              }
            }
          } else if (isVerbose()) {
            LOGGER.info("[sort] Warning. Declared decorator in " + identity + " is not found " + a);
          }
        }
      }

      if (before != null) {
        // for before order, we need to find the nodes that represent it and add the current node to its dependsOn field.
        for (Class b : before) {
          List<Decorator> beforeDecorators = dictionary.get(b);
          if (beforeDecorators != null) {
            for (Decorator beforeDecorator : beforeDecorators) {
              if (!beforeDecorator.getClass().equals(identity)) {
                Node<Decorator> beforeNode = unordered.get(beforeDecorator.getClass());
                if (beforeNode == null) {
                  beforeNode = new Node<>(beforeDecorator.getClass());
                  unordered.put(beforeDecorator.getClass(), beforeNode);
                }

                beforeNode.getDepends().add(identity);
                // to avoid
                node.getDepends().remove(beforeDecorator.getClass());
              }
            }
          } else if (isVerbose()) {
            LOGGER.info("[sort] Warning. Declared decorator in " + identity + " is not found " + b);
          }
        }
      }
    }

    return sort(unordered.values());
  }

  /**
   * Sort nodes with dependencies using a Graph Topological algorithm plus the visitor pattern to avoid having cycles and
   * to gain better performance.
   */
  private static <T extends Comparable> List<T> sort(Collection<Node<T>> items) {
    List<T> ordered = new ArrayList<>(items.size());
    Set<Object> visited = new HashSet<>();
    Set<Node<T>> cycle = new HashSet<>();
    ArrayDeque<Node<T>> back = new ArrayDeque<>();
    back.addAll(items);

    while (!back.isEmpty()) {
      Node<T> node = back.pop();
      Object identity = node.getValue();
      if (isVerbose()) {
        LOGGER.info("[sort] Sort " + identity);
      }
      List<Object> dependants = node.getDepends();
      if (dependants == null) {
        cycle.clear();
        visited.add(identity);
        ordered.addAll(node.getReferences());
      } else {
        boolean ready = true;
        if (dependants != null) {
          for (Object d : dependants) {
            if (!visited.contains(d)) {
              ready = false;
              if (isVerbose()) {
                LOGGER.info("[sort] " + identity + " is not ready because it needs " + d);
              }
              break;
            }
          }
        }

        if (ready) {
          cycle.clear();
          visited.add(identity);
          ordered.addAll(node.getReferences());
        } else if (cycle.contains(node)) {
          // Let's add the item with more dependants resolved.
          Node<T> nodeToAdd = null;
          Long maxResolvedDependants = null;
          for (Node<T> nodeInCycle : cycle) {
            long currentResolvedDependants = nodeInCycle.depends.stream().filter(visited::contains).count();
            if (nodeToAdd == null || maxResolvedDependants < currentResolvedDependants) {
              nodeToAdd = nodeInCycle;
              maxResolvedDependants = currentResolvedDependants;
            }
          }

          if (isVerbose()) {
            LOGGER.info("[sort] Warning. Cycle found: " + cycle);
            LOGGER.info("[sort] Warning. Cycle found. Adding: " + nodeToAdd.value);
          }

          cycle.clear();
          if (nodeToAdd.value.equals(identity)) {
            visited.add(identity);
            ordered.addAll(node.getReferences());
          } else {
            back.addLast(node);

            visited.add(nodeToAdd.value);
            ordered.addAll(nodeToAdd.getReferences());
          }

        } else {
          cycle.add(node);
          back.addLast(node);
        }
      }
    }

    return ordered;
  }

  /**
   * This class is internally used to represent a node with dependencies.
   */
  private static class Node<T> {
    private final Object value;

    private List<T> references = new ArrayList<>();
    private List<Object> depends = new ArrayList<>();

    public Node(Object value) {
      this.value = value;
    }

    public Node(Object value, T reference) {
      this.value = value;
      this.references.add(reference);
    }

    public Object getValue() {
      return value;
    }

    public List<T> getReferences() {
      return references;
    }

    public List<Object> getDepends() {
      return depends;
    }

    public void setDepends(List<Object> depends) {
      this.depends = depends;
    }
  }

  private static Map<Class, List<Decorator>> createDictionary(Collection<Decorator> items) {
    Map<Class, List<Decorator>> dictionary = new HashMap<>();
    for (Decorator item : items) {
      Class<?> clazz = item.getClass();
      while (!clazz.equals(Decorator.class)) {
        List<Decorator> list = dictionary.get(clazz);
        if (list == null) {
          list = new ArrayList<>();
          dictionary.put(clazz, list);
        }
        if (isVerbose()) {
          LOGGER.info("Sort: mapping " + clazz + " with " + item);
        }
        list.add(item);
        clazz = clazz.getSuperclass();
      }
    }

    return dictionary;
  }

}
