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
    Collection<Node> nodes = adaptToNodes(items);
    return sortNodes(nodes);
  }

  /**
   * This method will adapt the current Decorator class into a collection of nodes.
   * For each Decorator:
   * - If the decorator X needs to be triggered after Y; then the node X which depends on Y.
   * - If the decorator X needs to be triggered before Y; then the node Y which depends on X.
   */
  private static Collection<Node> adaptToNodes(Collection<Decorator> items) {
    // This dictionary is used to verify a decorator is in place and register the hierarchy.
    Dictionary dictionary = new Dictionary(items);
    Map<Class, Node> unordered = new HashMap<>();
    for (Decorator item : items) {
      Class identity = item.getClass();
      Class[] after = item.after();
      Class[] before = item.before();
      Node node = getOrCreate(identity, unordered);

      node.references.add(item);
      if (after != null) {
        // the after order is the same as the dependsOn order, so we simply add it.
        for (Class a : after) {
          List<Decorator> afterDecorators = dictionary.lookup(a);
          if (afterDecorators != null) {
            for (Decorator afterDecorator : afterDecorators) {
              if (!afterDecorator.getClass().equals(identity)) {
                node.depends.add(afterDecorator.getClass());
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
          List<Decorator> beforeDecorators = dictionary.lookup(b);
          if (beforeDecorators != null) {
            for (Decorator beforeDecorator : beforeDecorators) {
              if (!beforeDecorator.getClass().equals(identity)) {
                Node beforeNode = getOrCreate(beforeDecorator.getClass(), unordered);
                beforeNode.depends.add(identity);
                // to avoid
                node.depends.remove(beforeDecorator.getClass());
              }
            }
          } else if (isVerbose()) {
            LOGGER.info("[sort] Warning. Declared decorator in " + identity + " is not found " + b);
          }
        }
      }
    }
    return unordered.values();
  }

  /**
   * Sort nodes with dependencies using a Graph Topological algorithm plus the visitor pattern to avoid having cycles and
   * to gain better performance.
   */
  private static List<Decorator> sortNodes(Collection<Node> items) {
    List<Decorator> ordered = new ArrayList<>(items.size());
    Set<Class> visited = new HashSet<>();
    Set<Node> cycle = new HashSet<>();
    // First, we loop over each node in order of insertion.
    ArrayDeque<Node> back = new ArrayDeque<>();
    back.addAll(items);

    while (!back.isEmpty()) {
      // Get the first node.
      Node node = back.pop();
      Class identity = node.value;
      if (isVerbose()) {
        LOGGER.info("[sort] Sort " + identity);
      }
      List<Class> dependants = node.depends;
      if (dependants == null) {
        cycle.clear();
        visited.add(identity);
        ordered.addAll(node.references);
      } else {
        boolean ready = true;
        for (Class d : dependants) {
          if (!visited.contains(d)) {
            ready = false;
            if (isVerbose()) {
              LOGGER.info("[sort] " + identity + " is not ready because it needs " + d);
            }
            break;
          }
        }

        if (ready) {
          // if all the node dependencies have been seen/visited, we can include the current node into the ordered list.
          cycle.clear();
          visited.add(identity);
          ordered.addAll(node.references);
        } else if (cycle.contains(node)) {
          // Let's add the item with more dependants resolved.
          Node nodeToAdd = null;
          Long maxResolvedDependants = null;
          for (Node nodeInCycle : cycle) {
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
            ordered.addAll(node.references);
          } else {
            back.addLast(node);

            visited.add(nodeToAdd.value);
            ordered.addAll(nodeToAdd.references);
          }

        } else {
          // If there are still missing dependencies, put back the current node into the back of the queue and continue.
          cycle.add(node);
          back.addLast(node);
        }
      }
    }

    return ordered;
  }

  /**
   * Get the node matching the class or create one if there is no node yet.
   */
  private static Node getOrCreate(Class clazz, Map<Class, Node> unordered) {
    Node node = unordered.get(clazz);
    if (node == null) {
      node = new Node(clazz);
      unordered.put(clazz, node);
    }

    return node;
  }

  /**
   * This class is internally used to represent a node of decorators and its dependencies.
   */
  private static class Node {
    private final Class value;

    private List<Decorator> references = new ArrayList<>();
    private List<Class> depends = new ArrayList<>();

    public Node(Class value) {
      this.value = value;
    }
  }

  /**
   * This class acts as a dictionary of decorators and its class hierarchy.
   */
  private static class Dictionary {
    private final Map<Class, List<Decorator>> dictionary;

    public Dictionary(Collection<Decorator> items) {
      this.dictionary = new HashMap<>();
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
    }

    public List<Decorator> lookup(Class clazz) {
      return dictionary.get(clazz);
    }
  }

}
