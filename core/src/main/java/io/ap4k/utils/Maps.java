/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package io.ap4k.utils;

import io.ap4k.Ap4kException;
import io.ap4k.deps.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class Maps {
  private static final String PROPERTY_PREFIX = "ap4k";
  private static final String MULTIPART_SEPARATOR_PATTERN = Pattern.quote(".");
  private static final String ARRAY_PATTERN = "[a-zA-Z0-9_]+\\[\\d+\\]";

  /**
   * Read a properties input stream and crate a configuration map.
   * The configuration map follows all the required conventions in order to be usable by a Generator.
   *
   * @return a {@link Map} with in the Generator format.
   */
  public static Map<String, Object> fromProperties(InputStream is) {
    Map<String, Object> result = new HashMap<>();
    Properties properties = new Properties();
    try {
      properties.load(is);
    } catch (IOException e) {
      throw Ap4kException.launderThrowable(e);
    }

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
  private static void merge(Map<String, Object> existing, Map<String, Object> map) {
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

  private static void unrollArrays(Map<String, Object> result) {
    Map<String, Object> copy = new HashMap<>(result);
    for (Map.Entry<String, Object> entry : copy.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (value instanceof Map) {
        unrollArrays((Map<String, Object>) value);
      }
      if (key.matches(ARRAY_PATTERN)) {
        String strippedKey = key.substring(0, key.indexOf("["));
        List<Object> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (int i=0;result.containsKey(strippedKey+"["+i+"]");i++) {
          String currentKey = strippedKey + "[" + i + "]";
          Object obj = result.get(currentKey);
          list.add(obj);
          result.remove(currentKey);
        }

        if (!list.isEmpty()) {
          result.put(strippedKey , list);
        }
      }
    }
  }
}
