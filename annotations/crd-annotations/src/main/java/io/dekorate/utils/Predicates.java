
package io.dekorate.utils;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import java.util.function.Predicate;

public class Predicates {

  /**
   * Creates a {@link Predicate} for {@link io.dekorate.kubernetes.config.Container}.
   */
  public static Predicate<Container> matches(io.dekorate.kubernetes.config.Container container) {
    return new Predicate<Container>() {
      @Override
      public boolean test(Container c) {
        return Strings.isNullOrEmpty(c.getName()) || c.getName().equals(container.getName());
      }
    };
  }

  /**
   * Creates a {@link Predicate} for {@link Container}.
   */
  public static Predicate<Container> matches(Container container) {
    return new Predicate<Container>() {
      @Override
      public boolean test(Container c) {
        return Strings.isNullOrEmpty(c.getName()) || c.getName().equals(container.getName());
      }
    };
  }


  /**
   * Creates a {@link java.util.function.Predicate} for {@link ContainerBuilder}.
   */
  public static java.util.function.Predicate<ContainerBuilder> builderMatches(io.dekorate.kubernetes.config.Container container) {
    return new java.util.function.Predicate<ContainerBuilder>() {
      @Override
      public boolean test(ContainerBuilder builder) {
        return matches(container).test(builder.build());
      }
    };
  }

  /**
   * Creates a {@link java.util.function.Predicate} for {@link ContainerBuilder}.
   */
  public static java.util.function.Predicate<ContainerBuilder> builderMatches(Container container) {
    return new java.util.function.Predicate<ContainerBuilder>() {
      @Override
      public boolean test(ContainerBuilder builder) {
        return matches(container).test(builder.build());
      }
    };
  }


  /**
   * Creates a {@link Predicate} for {@link io.dekorate.kubernetes.config.Port}.
   */
  public static Predicate<ContainerPort> matches(io.dekorate.kubernetes.config.Port port) {
    return new Predicate<ContainerPort>() {
      @Override
      public boolean test(ContainerPort p) {
        if (Strings.isNullOrEmpty(p.getName())) {
          return p.getContainerPort().intValue() == port.getContainerPort();
        } else return p.getName().equals(port.getName());
      }
    };
  }

  /**
   * Creates a {@link java.util.function.Predicate} for {@link ContainerPortBuilder}.
   */
  public static java.util.function.Predicate<ContainerPortBuilder> builderMatches(io.dekorate.kubernetes.config.Port port) {
     return new java.util.function.Predicate<ContainerPortBuilder> () {
      @Override
      public boolean test(ContainerPortBuilder builder) {
        return matches(port).test(builder.build());
      }
    };
  }

}
