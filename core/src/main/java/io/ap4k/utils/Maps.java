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
 * 
**/

package io.ap4k.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import io.ap4k.Ap4kException;

public class Maps {

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
      Object value = properties.get(key);
      Map<String, Object> kv = asMap(k.split(Pattern.quote(".")), value);
      merge(result, kv);
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
    Object nested = asMap(remaining, value);
    result.put(key, nested);
    return result;
  }


  /**
   * Merge a nested map to an existing one.
   * @param An existing map.
   * @param The map that will be merged into the existing.
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
}
