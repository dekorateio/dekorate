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

import io.dekorate.utils.Generators;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Generates resources, based on the detected annotations.
 */
public interface Generator extends SessionHandler {

  Map<String, Generator> GENERATORS = new HashMap<>();
  Map<String, Class<? extends Annotation>> ANNOTATIONS = new HashMap<>();

  /**
   * @return all registered @{link Generator}s.
   */
  static List<Generator> getGenerators() {
    List<Generator> generators = new ArrayList<>();
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(Generator.class.getClassLoader());
      ServiceLoader<Generator> loader = ServiceLoader.load(Generator.class);
      for (Generator generator : loader) {
        generators.add(generator);
      }
    } finally {
      Thread.currentThread().setContextClassLoader(tccl);
    }
    return generators;
  }

  /**
   * Initialize
   */
  static void init(Map<String, Object> map) {
    // Lazy load generators
    getGenerators();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      Generator generator = getGenerator(key);
      if (generator == null) {
        throw new IllegalArgumentException("Unknown generator '" + key + "'. Known generators are: " + GENERATORS.keySet());
      }

      if (value instanceof Map) {
        Map<String, Object> generatorMap = new HashMap<>();
        Class annotationClass = ANNOTATIONS.get(key);
        String newKey = annotationClass.getName();
        Generators.populateArrays(annotationClass, (Map<String, Object>) value);
        generatorMap.put(newKey, value);
        generator.add(generatorMap);
      }
    }
  }

  static Class<? extends Annotation> registerAnnotationClass(String key, Class<? extends Annotation> type) {
    return ANNOTATIONS.put(key, type);
  }

  static Class<? extends Annotation> getAnnotationClass(String key) {
    return ANNOTATIONS.get(key);
  }

  static Generator registerGenerator(String key, Generator generator) {
    return GENERATORS.put(key, generator);
  }

  static Generator getGenerator(String key) {
    return GENERATORS.get(key);
  }

  /**
   * Add a map as the generator input.
   */
  void add(Map map);

  /**
   * Generate the resources. This method may be called multiple times, but should
   * only generate the resources once.
   */
  default void generate() {
    // do nothing
    session.close();
  }

  /**
   * Returns a list of the annotations that are supported by the generator This is
   * meant to be used by tools other than APT When the generator is extended by an
   * APT processor, this method is never consulted
   */
  default List<Class> getSupportedAnnotations() {
    return Collections.emptyList();
  }

  default Map<String, Object> filter(Map<String, Object> properties) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (ANNOTATIONS.containsKey(key)) {
        result.put(ANNOTATIONS.get(key).getName(), value);
      } else {
        result.put(key, value);
      }
    }
    return result;
  }

}
