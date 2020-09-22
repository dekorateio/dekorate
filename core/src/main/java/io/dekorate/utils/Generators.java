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
package io.dekorate.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Generators {

  private static Field findField(Class c, String field) throws NoSuchFieldError, SecurityException, NoSuchFieldException {
    return findField(c, c, field);
  }

  private static Field findField(Class c, Class origin, String field)
      throws NoSuchFieldError, SecurityException, NoSuchFieldException {
    try {
      return c.getDeclaredField(field);
    } catch (NoSuchFieldError | SecurityException | NoSuchFieldException e) {
      Class s = c.getSuperclass();
      if (s == null) {
        throw e;
      }
      return findField(s, origin, field);
    }
  }

  /**
   * Process the specified map and wrap sub-maps into arrays of maps when needed.
   * 
   * @param annotationClass The class of the annotation.
   * @param map The actual map.
   */
  public static void populateArrays(Class configClass, Map<String, Object> map) {
    for (Map.Entry<String, Object> entry : new HashMap<String, Object>(map).entrySet()) {
      String key = Strings.kebabToCamelCase(entry.getKey());
      Object value = entry.getValue();
      try {

        Class fieldType = findField(configClass, key).getType();
        if (value instanceof String && fieldType.isArray()) {
          String[] newValue = ((String) value).split("\\s*,\\s*");
          map.put(key, newValue);
        }
        if (value instanceof Map) {
          populateArrays(fieldType, (Map<String, Object>) value);
          if (fieldType.isArray()) {
            Map[] newValue = new Map[1];
            newValue[0] = (Map) value;
            map.put(key, newValue);
          }
        } else if (value instanceof Object[] && isMapArray((Object[]) value)) {
          Object[] objects = (Object[]) value;
          Map[] newValue = new Map[objects.length];
          System.arraycopy(objects, 0, newValue, 0, objects.length);
          map.put(key, newValue);
        } else if (value instanceof List && isMapList((List) value)) {
          List list = (List) value;
          Map[] newValue = new Map[list.size()];
          for (int i = 0; i < list.size(); i++) {
            newValue[i] = (Map) list.get(i);
          }
          map.put(key, newValue);
        }
      } catch (NoSuchFieldError | SecurityException | NoSuchFieldException e) {
        //ignore an move to next entry.
        continue;
      }
    }
  }

  /**
   * Check if specified Object array is actually an array of maps.
   * 
   * @param objects the object array.
   * @return true if all elements are instance of Map.
   */
  private static boolean isMapArray(Object[] objects) {
    for (Object o : objects) {
      if (!(o instanceof Map)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if specified list is actually a list of maps.
   * 
   * @param list the list..
   * @return true if all elements are instance of Map.
   */
  private static boolean isMapList(List<?> list) {
    for (Object o : list) {
      if (!(o instanceof Map)) {
        return false;
      }
    }
    return true;
  }

}
