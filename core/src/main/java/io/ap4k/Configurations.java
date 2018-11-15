package io.ap4k;

import io.ap4k.config.Configuration;
import io.ap4k.config.ConfigurationSupplier;
import io.fabric8.kubernetes.api.builder.Visitor;

import java.util.HashSet;
import java.util.LinkedHashSet;
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


}
