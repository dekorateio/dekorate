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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.dekorate.config.MultiConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.kubernetes.config.ApplicationConfiguration;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.utils.Beans;

public class ConfigurationRegistry {

  private final Map<Type, List<ConfigurationSupplier<? extends Configuration>>> suppliers = new ConcurrentHashMap<>();
  private final Set<Configurator> configurators = new HashSet<>();

  public void add(ConfigurationSupplier supplier) {
    Type type = supplier.getType();
    if (!suppliers.containsKey(type)) {
      suppliers.put(type, new ArrayList<>());
    }
    this.suppliers.get(supplier.getType()).add(supplier);
  }

  /**
   * Add a {@link Configurator}.
   * 
   * @param configurator The configurator.
   */
  public void add(Configurator configurator) {
    configurators.add(configurator);
  }

  public Set<Configurator> getConfigurators() {
    return configurators;
  }

  public Stream<? extends Configuration> stream() {
    return suppliers.values()
        .stream()
        .flatMap(l -> combineMulti(l.stream().map(s -> s.configure(configurators)).collect(Collectors.toList())).stream());
  }

  public boolean isEmpty() {
    return suppliers.isEmpty();
  }

  public Set<? extends Configuration> toSet() {
    return stream().collect(Collectors.toSet());
  }

  public <C extends Configuration> Optional<C> get(Class<C> type) {
    return get(type, c -> true);
  }

  public <C extends Configuration> Optional<C> get(Class<C> type, Predicate<C> predicate) {
    return stream().filter(i -> type.isInstance(i))
        .map(i -> (C) i)
        .filter(predicate)
        .findFirst();
  }

  public <C extends Configuration> List<C> getAll(Class<C> type) {
    return getAll(type, c -> true);
  }

  public <C extends Configuration> List<C> getAll(Class<C> type, Predicate<C> predicate) {
    return stream().filter(i -> type.isInstance(i))
        .map(i -> (C) i)
        .filter(predicate)
        .collect(Collectors.toList());
  }

  //
  // Application Config specifics
  //

  public boolean hasApplicationConfiguration() {
    return applicationConfigurationStream().count() > 0;
  }

  public Stream<? extends ApplicationConfiguration> applicationConfigurationStream() {
    return suppliers.values()
        .stream()
        .map(l -> combine(l.stream()
            .map(s -> s.configure(configurators))
            .filter(s -> s.get() instanceof ApplicationConfiguration)
            .map(s -> (ConfigurationSupplier<ApplicationConfiguration>) s)
            .collect(Collectors.toList())));
  }

  public Stream<? extends ApplicationConfiguration> applicationConfigurationStream(Predicate<ConfigurationSupplier<ApplicationConfiguration>> predicate) {
    return suppliers.values()
        .stream()
        .map(l -> combine(l.stream()
            .map(s -> s.configure(configurators))
            .filter(s -> s.get() instanceof ApplicationConfiguration)
            .map(s -> (ConfigurationSupplier<ApplicationConfiguration>) s)
            .filter(predicate)
            .collect(Collectors.toList())));
  }

  //
  // Image Config specifics
  //

  public boolean hasImageConfiguration() {
    return imageConfigurationStream().count() > 0;
  }

  public Stream<? extends ImageConfiguration> imageConfigurationStream() {
    return suppliers.values()
        .stream()
        .map(l -> combine(l.stream()
            .map(s -> s.configure(configurators))
            .filter(s -> s.get() instanceof ImageConfiguration)
            .map(s -> (ConfigurationSupplier<ImageConfiguration>) s)
            .collect(Collectors.toList())));
  }

  public Stream<? extends ImageConfiguration> imageConfigurationStream(Predicate<ConfigurationSupplier<ImageConfiguration>> predicate) {
    return suppliers.values()
        .stream()
        .map(l -> combine(l.stream()
            .map(s -> s.configure(configurators))
            .filter(s -> s.get() instanceof ImageConfiguration)
            .map(s -> (ConfigurationSupplier<ImageConfiguration>) s)
            .filter(predicate)
            .collect(Collectors.toList())));
  }

  public <C extends ImageConfiguration> Optional<C> getImageConfig(Class<C> type) {
    return getImageConfig(type, c -> true);
  }

  //Copy
  public Optional<ImageConfiguration> getImageConfig(Predicate<ConfigurationSupplier<ImageConfiguration>> predicate) {
    return imageConfigurationStream(predicate).filter(i -> i instanceof ImageConfiguration)
        .map(i -> (ImageConfiguration) i)
        .findFirst();
  }

  public <C extends ImageConfiguration> Optional<C> getImageConfig(Class<C> type, Predicate<C> predicate) {
    return imageConfigurationStream().filter(i -> type.isInstance(i))
        .map(i -> (C) i)
        .filter(predicate)
        .findFirst();
  }

  public <C extends ImageConfiguration> List<C> getAllImageConfigs(Class<C> type) {
    return getAllImageConfigs(type, c -> true);
  }

  public <C extends ImageConfiguration> List<C> getAllImageConfigs(Class<C> type, Predicate<C> predicate) {
    return imageConfigurationStream().filter(i -> type.isInstance(i))
        .map(i -> (C) i)
        .filter(predicate)
        .collect(Collectors.toList());
  }


  private static <C extends Configuration> C combine(ConfigurationSupplier<C> origin, ConfigurationSupplier<C> override) {
    return Beans.combine(origin.get(), override.get());
  }

  private static <C extends Configuration> List<C> combineMulti(List<ConfigurationSupplier<C>> suppliers) {
    Stream<ConfigurationSupplier<C>> multis = suppliers.stream().filter(s -> s instanceof MultiConfiguration);
    List<ConfigurationSupplier<C>> singles = suppliers.stream().filter(s -> !(s instanceof MultiConfiguration)).collect(Collectors.toList());
    List<C> result = new ArrayList<>();
    if (!singles.isEmpty()) {
      result.add(combine(singles));
    }
    result.addAll(multis.map(ConfigurationSupplier::get).collect(Collectors.toList()));
    return result;
  }

  private static <C extends Configuration> C combine(List<ConfigurationSupplier<C>> suppliers) {
    if (suppliers.isEmpty()) {
      return null;
    }

    if (suppliers.size() == 1) {
      return suppliers.get(0).get();
    }

    List<ConfigurationSupplier<C>> copy = new ArrayList<>(suppliers);
    Collections.sort(copy);
    ConfigurationSupplier<C> origin = copy.get(0);
    copy.remove(0);
    return Beans.combine(origin.get(), combine(copy));
  }
}
