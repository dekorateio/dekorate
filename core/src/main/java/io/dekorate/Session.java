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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.kubernetes.config.ApplicationConfiguration;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.ImageConfigurationFluent;
import io.dekorate.utils.Generators;
import io.dekorate.utils.Maps;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.KubernetesList;

/**
 * The object that holds the state used by all processors.
 * When the state is closed, the session returns multiple {@link KubernetesList} that are created using the following rules:
 *
 * 1. For each named group created by processors create a list will all items assigned explicitly to the group.
 * 2. Items added to no particular group, are appended to all groups.
 * 3. Visitors are applied to each group.
 *
 */
public class Session {

  private static Session INSTANCE;

  private final AtomicBoolean closed = new AtomicBoolean();
  private final AtomicBoolean generated = new AtomicBoolean();

  private Set<String> enabledGroups = new HashSet<>();
  private Set<String> disabledGroups = new HashSet<>();

  private final Set<ManifestGenerator> manifestGenerators = new TreeSet<>(Comparator.comparing(ManifestGenerator::order));

  private final Map<String, ConfigurationGenerator> configurationGenerators = new HashMap<>();
  private final Map<String, Class<? extends Configuration>> configtypes = new HashMap<>();

  private final ConfigurationRegistry configurationRegistry = new ConfigurationRegistry();
  private final ResourceRegistry resourceRegistry = new ResourceRegistry();

  private final Map<String, KubernetesList> generatedResources = new HashMap<>();
  private final AtomicReference<SessionReader> reader = new AtomicReference<>();
  private final AtomicReference<SessionWriter> writer = new AtomicReference<>();
  private final Map<Class<? extends SessionListener>, SessionListener> listeners = new HashMap<>();

  private final Logger LOGGER;

  /**
   * Creates or reuses a single instance of Session.
   * 
   * @return The Session.
   */
  public static Session getSession() {
    return getSession(LoggerFactory.getLogger());
  }

  /**
   * Creates or reuses a single instance of Session.
   * 
   * @return The Session.
   */
  public static Session getSession(Logger logger) {
    if (INSTANCE != null) {
      return INSTANCE;
    }
    synchronized (Session.class) {
      if (INSTANCE == null) {
        INSTANCE = new Session(logger);
        INSTANCE.loadManifestGenerators();
        INSTANCE.loadConfigurationGenerators();
        INSTANCE.loadListeners();
      }
    }
    return INSTANCE;
  }

  protected Session(Logger logger) {
    LoggerFactory.setLogger(logger);
    LOGGER = logger;
    LOGGER.info("Initializing dekorate session.");
  }

  public void loadManifestGenerators() {
    Iterator<ManifestGeneratorFactory> iterator = ServiceLoader.load(ManifestGeneratorFactory.class, Session.class.getClassLoader()).iterator();
    while (iterator.hasNext()) {
      this.manifestGenerators.add(iterator.next().create(this.resourceRegistry, this.configurationRegistry));
    }
  }

  public void loadConfigurationGenerators() {
    Iterator<ConfigurationGeneratorFactory> iterator = ServiceLoader.load(ConfigurationGeneratorFactory.class, Session.class.getClassLoader()).iterator();
    while (iterator.hasNext()) {
      ConfigurationGenerator g = iterator.next().create(this.configurationRegistry);
      if (g.getKey() != null) {
        this.configurationGenerators.put(g.getKey(), g);
        if (g.getConfigType() != null) {
          this.configtypes.put(g.getKey(), g.getConfigType());
        }
      }
    }
  }

  public void loadListeners() {
    Iterator<SessionListener> iterator = ServiceLoader.load(SessionListener.class, Session.class.getClassLoader()).iterator();
    while (iterator.hasNext()) {
      addListener(iterator.next());
    }
  }



  public void enable(String... groups) {
    for (String group : groups) {
      this.enabledGroups.add(group);
    }
  }

  public void disalbe(String... groups) {
    for (String group : groups) {
      this.disabledGroups.add(group);
    }
  }

  public void addAnnotationConfiguration(Map<String, Object> map) {
    addConfiguration(map, (g, m) -> g.addAnnotationConfiguration(m));
  }

  public void addPropertyConfiguration(Map<String, Object> map) {
    addConfiguration(map, (g, m) -> g.addPropertyConfiguration(m));
  }

  public void addConfiguration(Map<String, Object> map, BiConsumer<ConfigurationGenerator, Map<String, Object>> consumer) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      ConfigurationGenerator generator = configurationGenerators.get(key);
      if (generator == null) {
        throw new IllegalArgumentException(
            "Unknown generator '" + key + "'. Known generators are: " + configurationGenerators.keySet());
      }

      if (value instanceof Map) {
        Map<String, Object> generatorMap = new HashMap<>();
        Class configClass = configtypes.get(key);
        String newKey = configClass.getName();
        Generators.applyPrimitives(configClass, (Map<String, Object>) value);
        Generators.populateArrays(configClass, (Map<String, Object>) value);
        generatorMap.put(newKey, value);
        consumer.accept(generator, Maps.kebabToCamelCase(generatorMap));
      }
    }
  }

  private Map<String, Object> filter(Map<String, Object> properties) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (configtypes.containsKey(key)) {
        result.put(configtypes.get(key).getName(), value);
      } else {
        result.put(key, value);
      }
    }
    return result;
  }

  public void disable(String group) {
    configurationGenerators.remove(group);
  }

  //should be used only for testing
  public static void clearSession() {
    INSTANCE = null;
  }

  public ConfigurationRegistry getConfigurationRegistry() {
    return configurationRegistry;
  }

  public ResourceRegistry getResourceRegistry() {
    return resourceRegistry;
  }

  public Set<ManifestGenerator> getManifestGenerators() {
    return manifestGenerators;
  }

  public Map<String, KubernetesList> getGeneratedResources() {
    return generatedResources;
  }

  public boolean hasReader() {
    return reader.get() != null;
  }

  public void setReader(SessionReader sessionReader) {
    reader.set(sessionReader);
  }

  public void setWriter(SessionWriter resourceWriter) {
    this.writer.set(resourceWriter);
  }

  public boolean hasWriter() {
    return this.writer.get() != null;
  }

  public void addListener(SessionListener listener) {
    listeners.put(listener.getClass(), listener);
  }

  /**
   * @return Map containing the file system paths of the output files as keys and their actual content as the values
   */
  public Map<String, String> close() {
    if (closed.compareAndSet(false, true)) {
      generate();
      SessionWriter w = writer.get();
      if (w == null) {
        throw new IllegalStateException("No writer has been specified!");
      }
      final Map<String, String> result = w.write(this);
      listeners.values().stream().forEach(SessionListener::onClosed);
      LOGGER.info("Closing dekorate session.");
      return result;
    }

    return new HashMap<>();
  }

  /**
   * Close the session an get all resource groups.
   * 
   * @return A map of {@link KubernetesList} by group name.
   */
  private Map<String, KubernetesList> generate() {
    Set<ManifestGenerator> manifestGeneratorsToRemove = manifestGenerators.stream().filter(
        h -> disabledGroups.contains(h.getKey()) || (!enabledGroups.isEmpty() && !enabledGroups.contains(h.getKey())))
        .collect(Collectors.toSet());
    this.manifestGenerators.removeAll(manifestGeneratorsToRemove);

    Set<String> generatorsToRemove = configurationGenerators.keySet().stream()
        .filter(g -> disabledGroups.contains(g) || (!enabledGroups.isEmpty() && !enabledGroups.contains(g)))
        .collect(Collectors.toSet());
    generatorsToRemove.forEach(g -> configurationGenerators.remove(g));

    if (generated.compareAndSet(false, true)) {
      LOGGER.info("Generating manifests.");
      closed.set(true);
      readExistingResources();
      populateFallbackConfig();
      checkConfigurationConsistency();
      manifestGenerators.forEach(m -> generate(m, configurationRegistry));
      this.generatedResources.putAll(resourceRegistry.generate());
    }
    return Collections.unmodifiableMap(generatedResources);
  }

  private void readExistingResources() {
    Optional.ofNullable(reader.get()).ifPresent(sr -> sr.read(this));
  }

  private void populateFallbackConfig() {
    if (!hasApplicationConfiguration(configurationRegistry)) {
      manifestGenerators.stream().forEach(h -> {
        if (!hasMatchingConfiguration(h, configurationRegistry)) {
          ConfigurationSupplier<? extends Configuration> supplier = h.getFallbackConfig();
          if (supplier.hasConfiguration()) {
            configurationRegistry.add(supplier);
          }
        }
      });
    }
  }

  private void checkConfigurationConsistency() {
    Set<Coordinates> applicationConfigurations = configurationRegistry.stream()
      .filter(c -> (c instanceof ApplicationConfiguration) && !(c instanceof ImageConfiguration))
      .map(c -> (ApplicationConfiguration) c)
      .map(a -> new DefaultCoordinates(a.getPartOf(), a.getName(), a.getVersion()))
      .collect(Collectors.toSet());

    //If we have a single appliction configuration, we should apply it to ImageConfiguration using defaults.
    if (applicationConfigurations.size() == 1) {
      final Coordinates coords = applicationConfigurations.iterator().next();
      configurationRegistry.add(new Configurator<ImageConfigurationFluent>() {
          @Override
          public void visit(ImageConfigurationFluent imageConfiguration) {
            if (Strings.isNullOrEmpty(imageConfiguration.getGroup())) {
              imageConfiguration.withGroup(coords.getPartOf());
            }

            if (Strings.isNullOrEmpty(imageConfiguration.getName())) {
              imageConfiguration.withName(coords.getName());
            }
            if (Strings.isNullOrEmpty(imageConfiguration.getVersion())) {
              imageConfiguration.withVersion(coords.getVersion());
            }
          }
      });
    }
    
    configurationRegistry.imageConfigurationStream()
      .filter(i -> i != null)
      .forEach(i -> {
          Set<ApplicationConfiguration> matched = configurationRegistry.stream()
              .filter(c -> (c instanceof ApplicationConfiguration) && !(c instanceof ImageConfiguration))
              .map(c -> (ApplicationConfiguration) c)
              .filter(a -> a != null 
                        && ( !Strings.equals(i.getGroup(), a.getPartOf()) 
                             || !Strings.equals(i.getName(), a.getName())
                             || !Strings.equals(i.getVersion(), a.getVersion()))).collect(Collectors.toSet());

          if (matched.isEmpty()) {
            LOGGER.debug(String.format("No matching Application configuration found for Image configuration (group=%s,name=%s,version=%s). This is often leads to confusion!", i.getGroup(), i.getName(), i.getVersion()));
          } 
    });
  }

  private static void generate(ManifestGenerator h, ConfigurationRegistry configurationRegistry) {
    configurationRegistry.stream().forEach(c -> {
      if (h.accepts(c.getClass())) {
        h.generate(c);
      }
    });
  }

  private static boolean hasApplicationConfiguration(ConfigurationRegistry configurationRegistry) {
    return configurationRegistry.stream().anyMatch(c -> ApplicationConfiguration.class.isAssignableFrom(c.getClass()));
  }

  private static boolean hasMatchingConfiguration(ManifestGenerator h, ConfigurationRegistry configurationRegistry) {
    return configurationRegistry.stream().anyMatch(c -> h.accepts(c.getClass()));
  }
}
