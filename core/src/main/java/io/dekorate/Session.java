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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.kubernetes.config.ApplicationConfiguration;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.utils.Generators;

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

  private final Set<Handler> handlers = new TreeSet<>(Comparator.comparing(Handler::order));

  private final Map<String, Generator> generators = new HashMap<>();
  private final Map<String, Class<? extends Annotation>> annotations = new HashMap<>();

  private final Configurators configurators = new Configurators();
  private final Resources resources = new Resources();

  private final Map<String, KubernetesList> generatedResources= new HashMap<>();
  private final AtomicReference<SessionWriter> writer = new AtomicReference<>();
  private final Set<SessionListener> listeners = new LinkedHashSet<>();

  private final Logger LOGGER = LoggerFactory.getLogger();

  /**
   * Creates or reuses a single instance of Session.
   * @return  The Session.
   */
  public static Session getSession() {
    if (INSTANCE != null) {
      return INSTANCE;
    }
    synchronized (Session.class) {
      if (INSTANCE == null) {
        INSTANCE = new Session();
        INSTANCE.loadHandlers();
        INSTANCE.loadGenerators();
      }
    }
    return INSTANCE;
  }

  protected Session() {
    LOGGER.info("Initializing dekorate session.");
  }

  public void loadHandlers() {
    Iterator<HandlerFactory> iterator = ServiceLoader.load(HandlerFactory.class, Session.class.getClassLoader()).iterator();
    while(iterator.hasNext())  {
      this.handlers.add(iterator.next().create(this.resources, this.configurators));
    }
  }

  public void loadGenerators() {
    Iterator<Generator> iterator = ServiceLoader.load(Generator.class, Session.class.getClassLoader()).iterator();
    while (iterator.hasNext()) {
      Generator g = iterator.next();
      if (g.getKey() != null) {
        this.generators.put(g.getKey(), g);
        if (g.getAnnotation() != null) {
          this.annotations.put(g.getKey(), g.getAnnotation());
        }
      }
    }
  }

  public void feed(Map<String, Object> map) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      Generator generator = generators.get(key);
      if (generator == null) {
        throw new IllegalArgumentException("Unknown generator '" + key + "'. Known generators are: " + generators.keySet());
      }

      if (value instanceof Map) {
        Map<String, Object> generatorMap = new HashMap<>();
        Class annotationClass = annotations.get(key);
        String newKey = annotationClass.getName();
        Generators.populateArrays(annotationClass, (Map<String, Object>) value);
        generatorMap.put(newKey, value);
        generator.add(generatorMap);
      }
    }
  }

  private Map<String, Object> filter(Map<String, Object> properties) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (annotations.containsKey(key)) {
        result.put(annotations.get(key).getName(), value);
      } else {
        result.put(key, value);
      }
    }
    return result;
  }


  //should be used only for testing
  public static void clearSession() {
    INSTANCE = null;
  }

  public Configurators configurators() {
    return configurators;
  }

  public Resources resources() {
    return resources;
  }

  public Set<Handler> handlers() {
    return handlers;
  }

  public Map<String, KubernetesList> getGeneratedResources() {
    return generatedResources;
  }

  public void setWriter(SessionWriter resourceWriter) {
    this.writer.set(resourceWriter);
  }

  public boolean hasWriter() {
    return this.writer.get() != null;
  }

  public void addListener(SessionListener listener) {
    listeners.add(listener);
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
      listeners.forEach(SessionListener::onClosed);
      LOGGER.info("Closing dekorate session.");
      return result;
    }

    return new HashMap<>();
  }

  /**
   * Close the session an get all resource groups.
   * @return A map of {@link KubernetesList} by group name.
   */
  private Map<String, KubernetesList> generate() {
    if (generated.compareAndSet(false, true)) {
      LOGGER.info("Generating manifests.");
      closed.set(true);
      populateFallbackConfig();
      handlers.forEach(h -> handle(h, configurators));
      this.generatedResources.putAll(resources.generate());
    }
    return Collections.unmodifiableMap(generatedResources);
  }

  private void populateFallbackConfig() {
    if (!hasApplicationConfiguration(configurators)) {
      handlers.stream().forEach(h -> {
        if (!hasMatchingConfiguration(h, configurators)) {
          ConfigurationSupplier<? extends Configuration> supplier = h.getFallbackConfig();
          if (supplier.hasConfiguration()) {
            configurators.add(supplier);
          }
        }
      });
    }
  }

  private static void handle(Handler h, Configurators configurators) {
    configurators.stream().forEach(c -> {
      if (h.canHandle(c.getClass())) {
      h.handle(c);
    }
   });
  }
  private static boolean hasApplicationConfiguration(Configurators configurators) {
    return configurators.stream().anyMatch(c->ApplicationConfiguration.class.isAssignableFrom(c.getClass()));
  }

  private static boolean hasMatchingConfiguration(Handler h, Configurators configurators) {
    return configurators.stream().anyMatch(c->h.canHandle(c.getClass()));
  }

}
