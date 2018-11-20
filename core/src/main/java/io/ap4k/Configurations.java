package io.ap4k;

import io.ap4k.config.Configuration;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.deps.kubernetes.api.builder.Visitor;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Configurations {

  private final Set<ConfigurationSupplier<? extends Configuration>> suppliers = new LinkedHashSet<>();
  private final Set<Visitor> visitors = new HashSet<>();

  public void add(ConfigurationSupplier supplier) {
    this.suppliers.add(supplier);
  }
  /**
   * Add a {@link Visitor}.
   * @param visitor   The visitor.
   */
  public void accept(Visitor visitor) {
    visitors.add(visitor);
  }

  public Stream<? extends Configuration> stream() {
    return suppliers
      .stream()
      .map(s -> s.acceptAll(visitors).get());
  }

  public Set<? extends Configuration> toSet()  {
      return stream().collect(Collectors.toSet());
  }

  public <C extends Configuration> Optional<C> get(Class<C> type) {
    return stream().filter(i -> type.isAssignableFrom(i.getClass())).map(i -> (C)i).findFirst();
  }
}
