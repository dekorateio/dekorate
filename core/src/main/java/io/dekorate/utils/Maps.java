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

import io.dekorate.DekorateException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Maps {
  private static final String PROPERTY_PREFIX = "dekorate";
  private static final String MULTIPART_SEPARATOR_PATTERN = Pattern.quote(".");

  public static Map<String, String> from(String... values) {
    if (values.length == 0) {
      return Collections.emptyMap();
    } else if (values.length % 2 != 0) {
      throw new IllegalArgumentException("Expected an even number of arguments");
    }

    Map<String, String> result = new HashMap<>();
    for (int i = 0; i < values.length; i += 2) {
      result.put(values[i], values[i + 1]);
    }
    return result;
  }

  /**
   * Read a Map representing a properties file and create a config map.
   * The method performs the following conversions:
   * 1. Unrolls nested entries (keys separated with dots) into nested maps.
   * 2. Unrolls arrays.
   * The configuration map follows all the required conventions in order to be usable by a Generator.
   * @return a {@link Map} with in the Generator format.
   */
  public static Map<String, Object> fromProperties(Map<String, Object> properties) {
    Map<String, Object> result = new HashMap<>();

    for (Object key : properties.keySet()) {
      String k = String.valueOf(key);
      // only process entries with the property prefix
      if (k.startsWith(PROPERTY_PREFIX)) {
        Object value = properties.get(key);
        // drop the prefix and then proceed
        final String[] split = k.split(MULTIPART_SEPARATOR_PATTERN);
        if (split.length == 1) {
          throw new IllegalArgumentException("Invalid entry '" + k + "=" + value + "'. Must provide generator key");
        }
        final String[] parts = new String[split.length - 1];
        System.arraycopy(split, 1, parts, 0, split.length - 1);

        Map<String, Object> kv = asMap(parts, value);
        merge(result, kv);
      }
    }
    // Second pass unroll arrays
    unrollArrays(result);
    return result;
 
  }
 
  /**
   * Read a properties input stream and crate a configuration map.
   * The configuration map follows all the required conventions in order to be usable by a Generator.
   *
   * @return a {@link Map} with in the Generator format.
   */
  public static Map<String, Object> fromProperties(InputStream is) {
    Properties properties = new Properties();
    try {
      properties.load(is);
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
    return fromProperties(properties.entrySet().stream().collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> e.getValue())));
  }

  /**
   * Read a yaml input stream and crate a configuration map.
   * The configuration map follows all the required conventions in order to be usable by a Generator.
   *
   * @return a {@link Map} with in the Generator format.
   */
  public static Map<String, Object> fromYaml(InputStream is) {
    Map<String, Object> result = new HashMap<>();
    Map<Object, Object> yaml = Serialization.unmarshal(is, new TypeReference<Map<Object, Object>>() {
    });
    // only deal with prefixed object and move everything one level up
    final Object prefixed = yaml.get(PROPERTY_PREFIX);
    if (prefixed != null) {
      Map<Object, Object> valueAsMap = (Map<Object, Object>) prefixed;
      for (Map.Entry<Object, Object> entry : valueAsMap.entrySet()) {
        // value should be a Map<String, Object>
        Map<String, Object> kv = asMap(new String[]{String.valueOf(entry.getKey())}, entry.getValue());
        merge(result, kv);
      }
    }
    return result;
  }

 public static <A extends Annotation> Map<String, Object> fromAnnotation(String root, A annotation, Class<? extends A> type) {
    Map<String, Object> result = new HashMap<>();
    result.put(root, fromAnnotation(annotation, type));
    return result;
  }

  public static <A extends Annotation> Map<String, Object> fromAnnotation(A annotation, Class<? extends A> type) {
    Map<String, Object> result = new HashMap<>();
    try {
      for (Method m : type.getDeclaredMethods()) {
        Object value = m.invoke(annotation);
        Class<?> clazz = m.getReturnType();
        if (clazz.isArray()) {
          Class componentType = clazz.getComponentType();
          if (componentType.isAnnotation()) {
            List<Map<String, Object>> maps = new ArrayList<>();
            for (Object o : (Object[])value) {
              Map<String, Object> nested = fromAnnotation((Annotation)o, componentType);
              maps.add(nested);
            }
            result.put(m.getName(), maps.toArray(new Map[maps.size()]));
          } else if (((Object[])value).length == 0) {
            //let's skip empty arrays
          } else {
            result.put(m.getName(), Arrays.stream((Object[])value).map(String::valueOf).collect(Collectors.joining(",")));
          }
        } else if (clazz.isAnnotation()) {
            result.put(m.getName(), fromAnnotation((Annotation) value, (Class) clazz));
        } else {
          result.put(m.getName(), String.valueOf(value));
        }
      }
    } catch (Exception e)  {
      throw DekorateException.launderThrowable(e);
    }
    return result;
  }

  
  /**
   * Convert a multipart-key value pair to a Map.
   */
  private static Map<String, Object> asMap(String[] keys, Object value) {
    if (keys == null || keys.length == 0) {
      return null;
    }

    Map<String, Object> result = new HashMap<>();
    if (keys.length == 1) {
      result.put(keys[0], value);
      return result;
    }

    String key = keys[0];
    String[] remaining = new String[keys.length - 1];
    System.arraycopy(keys, 1, remaining, 0, remaining.length);
    Map<String, Object> nested = asMap(remaining, value);
    result.put(key, nested);
    return result;
  }

  /**
   * Merge a nested map to an existing one.
   *
   * @param existing the existing map.
   * @param map      the map that will be merged into the existing.
   */
  public static void merge(Map<String, Object> existing, Map<String, Object> map) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();

      Object existingValue = existing.get(key);
      if (existingValue == null) {
        existing.put(key, value);
      } else if (existingValue instanceof Map && value instanceof Map) {
        merge((Map<String, Object>) existingValue, (Map<String, Object>) value);
      } else {
        existing.put(key, value);
      }
    }
  }

  /**
   * Recursively convert all {@link Map} keys from kebab case to camel case.
   * Recursively here means that if a value is a {@link Map} it will also be converted.
   * @param The input string.
   * @return The camel cased string.
   */
  public static <T> Map<String, T> kebabToCamelCase(Map<String, T> map) {
    Map<String, T> result = new HashMap<>();
    for (Map.Entry<String, T> entry : map.entrySet()) {
      String key = entry.getKey();
      T value = entry.getValue();
      String newKey = Strings.kebabToCamelCase(key);
      T newValue = value;
      if (newValue instanceof Map) {
        newValue = (T) kebabToCamelCase((Map) newValue);
      } else if (newValue instanceof List) {
        List newList = new ArrayList<>();
        for (Object item : (List) newValue) {
          if (item instanceof Map) {
            newList.add(kebabToCamelCase((Map<String, Object>) item));
          } else {
            newList.add(item);
          }
        }
        newValue = (T) newList;
      } else if (newValue.getClass().isArray()) {
        List<T> newList = new ArrayList<>();
        Arrays.stream((T[]) newValue).forEach(item -> {
           if (item instanceof Map) {
            newList.add((T)kebabToCamelCase((Map) item));
          } else {
            newList.add(item);
          }
        });
        newValue = (T) newList.toArray((T[]) Arrays.copyOf((T[]) newValue, 0));
      }
      result.put(newKey, newValue);
    }
    return result;
  }

  private static void unrollArrays(Map<String, Object> result) {
     Map<String, Object> copy = new HashMap<>(result);
    for (Map.Entry<String, Object> entry : copy.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (value instanceof Map) {
        unrollArrays((Map<String, Object>) value);
      }
      if (key.contains("[") && key.contains("]")) {
        String strippedKey = key.substring(0, key.indexOf("["));
        List<Object> list = new ArrayList<>();
        List<Map> listOfMap = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (int i=0;result.containsKey(strippedKey+"["+i+"]");i++) {
          String currentKey = strippedKey + "[" + i + "]";
          Object obj = result.get(currentKey);
          if (obj instanceof Map) {
            listOfMap.add((Map) obj);
          } else {
            list.add(obj);
          }
          result.remove(currentKey);
        }

        if (!list.isEmpty()) {
          result.put(strippedKey , list);
        }

        if (!listOfMap.isEmpty()) {
          result.put(strippedKey , listOfMap.toArray(new Map[listOfMap.size()]));
        }
      }
    }
  }
}
