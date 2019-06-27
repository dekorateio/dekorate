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
package io.ap4k;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Map;

public interface SessionHandler extends WithSession {

  /**
   * Add an {@link Element} to the generator.
   * @param element The element.
   */
  default void add(Element element) {
  }

  /**
   * Add a {@link Map} to the generator.
   * @param map The map.
   */
  default void add(Map map) {
  }

  /**
   * Get the annotation properties {@link Map} that matches, to the specified {@link Annotation} type.
   * @param map     The source map.
   * @param type    The annotation type.
   * @return        The the properties map.
   */
  default Map propertiesMap(Map map, Class<? extends Annotation> type)  {
    if (map == null) {
      throw new NullPointerException("Map cannot be null.");
    }
    String key = type.getName();
    if (!map.containsKey(key)) {
      throw new IllegalStateException("The specified map doesn't contain the required key:"+ key+".");
    }
    Object o = map.get(key);
    if (o instanceof Map) {
      return (Map)o;
    }
    throw new ClassCastException("The value found for key:"+key+" was expected to be a java.util.Map, but was "+o.getClass().toString()+".");
  }

}
