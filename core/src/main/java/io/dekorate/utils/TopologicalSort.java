package io.dekorate.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.dekorate.kubernetes.decorator.Decorator;

public final class TopologicalSort {

  private static class Edge {

    private final Decorator<?> from;
    private final Decorator<?> to;

    public Edge(Decorator<?> from, Decorator<?> to) {
      this.from = from;
      this.to = to;
    }

    public Decorator<?> getFrom() {
      return from;
    }

    public Decorator<?> getTo() {
      return to;
    }

    @Override
    public String toString() {
      return "[" + from + " -> " + to + "]";
    }
  }

  private TopologicalSort() {
    // Utility class
  }

  /**
   * Perfom a topological sort to the specified decorators.
   * The method is using khan's algorithm and goes like this:
   *
   * L := Empty list that will contain the sorted elements
   * S := Set of all nodes with no incoming edge
   * while S is not empty do
   * remove a node n from S
   * add n to L
   * for each node m with an edge e from n to m do
   * remove edge e from the graph
   * if m has no other incoming edges then
   * insert m into S
   * if graph has edges then
   * return error (graph has at least one cycle)
   * else
   * return L
   *
   * @param the decorators to sort.
   * @return the sorted array.
   */
  public static Decorator<?>[] sort(Decorator<?>[] decorators) {
    List<Decorator<?>> L = new ArrayList<>();
    Set<Edge> graph = createGraph(decorators);
    Queue<Decorator<?>> S = new LinkedList<>();
    S.addAll(findRoots(graph));
    S.addAll(findDisconnected(decorators, graph));

    while (!S.isEmpty()) {
      Decorator<?> n = S.poll();
      L.add(n);

      List<Edge> connections = graph.stream().filter(e -> e.getFrom().equals(n)).collect(Collectors.toList());
      graph.removeAll(connections);

      for (Edge connection : connections) {
        Decorator<?> m = connection.getTo();
        if (!graph.stream().anyMatch(c -> c.getTo().equals(m))) {
          if (!S.contains(m)) {
            S.add(m);
          }
        }
      }
    }
    return L.toArray(new Decorator<?>[L.size()]);
  }

  /**
   * Create graph in the form of a {@link Set} of {@link Edge}(s)
   * 
   * @retun the set of edges.
   */
  public static Set<Edge> createGraph(Decorator<?>[] decorators) {
    return Stream.concat(
        Arrays.stream(decorators).flatMap(d -> edgesFrom(d, decorators).stream()),
        Arrays.stream(decorators).flatMap(d -> edgesTo(d, decorators).stream()))
        .collect(Collectors.toSet());
  }

  /**
   * Find he root root of the specified graph.
   * Root are the decorators that should be applied first.
   * Note: Decorators that are disconnected are not included (see findDisconnected).
   * 
   * @param graph The graph
   * @return a set with all the root decrotors
   */
  public static Set<Decorator<?>> findRoots(Set<Edge> graph) {
    return graph.stream().map(Edge::getFrom).filter(c -> !graph.stream().anyMatch(o -> o.getTo().equals(c)))
        .collect(Collectors.toSet());
  }

  /**
   * Find all the decorators that are not represnted in any edges in the graph.
   * 
   * @param decorators The decorators to check
   * @param graph The set of edges
   * @retun a set of disconncted node.s
   */
  public static Set<Decorator<?>> findDisconnected(Decorator<?>[] decorators, Set<Edge> graph) {
    return Arrays.stream(decorators).filter(d -> !graph.stream().anyMatch(e -> e.getFrom().equals(d) && e.getTo().equals(d)))
        .collect(Collectors.toSet());
  }

  /*
   * Get all the edges that lead the specified the decorator
   * 
   * @param decorator The decorator
   * 
   * @param decorators All decorators
   * 
   * @return A set containing all the edges to the decorator
   */
  public static Set<Edge> edgesTo(Decorator<?> decorator, Decorator<?>[] decorators) {
    Set<Edge> edges = new HashSet<>();
    for (Decorator<?> other : decorators) {
      if (Arrays.stream(decorator.after()).anyMatch(c -> c.equals(other.getClass()))) {
        edges.add(new Edge(other, decorator));
      }
      if (Arrays.stream(other.before()).anyMatch(c -> c.equals(decorator.getClass()))) {
        edges.add(new Edge(decorator, other));
      }
    }
    return edges;
  }

  /*
   * Get all the edges that originate from the specified the decorator
   * 
   * @param decorator The decorator
   * 
   * @param decorators All decorators
   * 
   * @return A set containing all the edges originating from the decorator
   */
  public static Set<Edge> edgesFrom(Decorator<?> decorator, Decorator<?>[] decorators) {
    Set<Edge> edges = new HashSet<>();
    for (Decorator<?> other : decorators) {
      if (Arrays.stream(other.after()).anyMatch(c -> c.equals(decorator.getClass()))) {
        edges.add(new Edge(decorator, other));
      }

      if (Arrays.stream(decorator.before()).anyMatch(c -> c.equals(other.getClass()))) {
        edges.add(new Edge(other, decorator));
      }
    }
    return edges;
  }
}
