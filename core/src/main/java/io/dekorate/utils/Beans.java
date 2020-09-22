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

package io.dekorate.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.dekorate.DekorateException;

public class Beans {

  /**
   * Combines two objects.
   * Combined object contains origin values overridden with the ones found in override Object (when not null).
   * Arrays and collections are merged, but items with matching `name` or `id` are combined into one before added to the
   * merged collection/array.
   * 
   * @param The object two use as origin.
   * @param The object with override values.
   */
  public static <C> C combine(C origin, C override) {
    if (origin == null) {
      return override;
    }

    if (override == null) {
      return origin;
    }

    Class<C> originClass = (Class<C>) origin.getClass();
    Class<C> overrideClass = (Class<C>) override.getClass();

    if (String.class.isAssignableFrom(originClass)) {
      if (Strings.isNullOrEmpty((String) origin)) {
        return override;
      }

      if (Strings.isNullOrEmpty((String) override)) {
        return origin;
      }
    }

    if (!overrideClass.equals(originClass)) {
      throw new IllegalStateException(
          String.format("Objects types don't match. Found: [%s] and [%s].", overrideClass, originClass));
    }

    if (overrideClass.isPrimitive() || overrideClass.isEnum()) {
      return override;
    }

    // Merge Lists
    if (List.class.isAssignableFrom(originClass)) {
      return mergeList(origin, override);
    }

    // Merge Sets
    if (Set.class.isAssignableFrom(originClass)) {
      return mergeSet(origin, override);
    }

    // Merge Maps
    if (Map.class.isAssignableFrom(originClass)) {
      return mergeMap(origin, override);
    }

    if (originClass.isArray()) {
      return mergeArray(origin, override);
    }

    String fcqn = overrideClass.getCanonicalName();
    if (fcqn.startsWith("java") || fcqn.startsWith("com.sun") || fcqn.startsWith("sun.")) {
      return override;
    }

    try {
      final C result = originClass.newInstance();
      for (Field f : getAllFields(originClass)) {
        try {
          String name = f.getName();
          f.setAccessible(true);
          Object value = combine(f.get(origin), f.get(override));
          f.set(result, value);
        } catch (Exception e) {
          throw DekorateException.launderThrowable(e);
        }
      }

      return result;
    } catch (Exception e) {
      throw DekorateException.launderThrowable(e);
    }

  }

  private static List<Field> getAllFields(Class clazz) {
    if (clazz == null) {
      return Collections.emptyList();
    }

    List<Field> result = new ArrayList<>(getAllFields(clazz.getSuperclass()));
    List<Field> filteredFields = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toList());
    result.addAll(filteredFields);
    return result;
  }

  /**
   * Merges two instances of {@link List}.
   * 
   * @param origin The original list.
   * @param override The override list.
   * @return The combined list.
   */
  private static <C> C mergeList(C origin, C override) {
    try {
      Class<C> originClass = (Class<C>) origin.getClass();
      List result = (List) originClass.newInstance();
      List originList = new ArrayList((List) origin);
      List overrideList = new ArrayList((List) override);
      for (Object o : (List) origin) {
        Object matching = findMatching(o, overrideList);
        if (matching != null) {
          originList.remove(o);
          overrideList.remove(matching);
          result.add(combine(o, matching));
        }
      }
      result.addAll(originList);
      result.addAll(overrideList);
      return (C) result;
    } catch (Exception e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  /**
   * Merges two arrays.
   * 
   * @param origin The original array.
   * @param override The override array.
   * @return The combined array.
   */
  private static <C> C mergeArray(C origin, C override) {
    try {
      Class originClass = origin.getClass().getComponentType();
      List result = new ArrayList<>();
      //We wrap the Arrays.asList() in an ArrayList so that we can mutate it.
      List originList = new ArrayList(Arrays.asList((Object[]) origin));
      List overrideList = new ArrayList(Arrays.asList((Object[]) override));
      for (Object o : (Object[]) origin) {
        Object matching = findMatching(o, overrideList);
        if (matching != null) {
          originList.remove(o);
          overrideList.remove(matching);
          result.add(combine(o, matching));
        }
      }
      result.addAll(originList);
      result.addAll(overrideList);
      return (C) result.toArray((Object[]) Array.newInstance(originClass, result.size()));
    } catch (Exception e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  /**
   * Merges two instances of {@link Set}.
   * 
   * @param origin The original set.
   * @param override The override set.
   * @return The combined set.
   */
  private static <C> C mergeSet(C origin, C override) {
    try {
      Class<C> originClass = (Class<C>) origin.getClass();
      Set originSet = new HashSet((Set) origin);
      Set overrideSet = new HashSet((Set) override);
      Set result = (Set) originClass.newInstance();
      for (Object o : (Set) origin) {
        Object matching = findMatching(o, overrideSet);
        if (matching != null) {
          originSet.remove(o);
          overrideSet.remove(matching);
          result.add(combine(o, matching));
        }
      }
      result.addAll(originSet);
      result.addAll(overrideSet);
      return (C) result;
    } catch (Exception e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  /**
   * Merges two instances of {@link Map}.
   * 
   * @param origin The original map.
   * @param override The override map.
   * @return The combined map.
   */
  private static <C> C mergeMap(C origin, C override) {
    try {
      Class<C> originClass = (Class<C>) origin.getClass();
      Map originMap = new HashMap((Map) origin);
      Map overrideMap = new HashMap((Map) override);
      Map result = (Map) originClass.newInstance();
      for (Object k : ((Map) origin).keySet()) {
        if (overrideMap.containsKey(k)) {
          Object originValue = originMap.get(k);
          Object overrideValue = overrideMap.get(k);
          originMap.remove(k);
          overrideMap.remove(k);
          result.put(k, combine(originValue, overrideValue));
        }
      }
      result.putAll(originMap);
      result.putAll(overrideMap);
      return (C) result;
    } catch (Exception e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  private static <C> C findMatching(C obj, Collection<C> col) {
    for (C o : col) {
      if (matches(obj, o)) {
        return o;
      }
    }
    return null;
  }

  private static <C> boolean matches(C obj, C other) {
    return (obj != null && obj.equals(other))
        || fieldEquals("id", obj, other)
        || fieldEquals("name", obj, other);
  }

  private static <C> boolean fieldEquals(String name, C obj, C other) {
    Class type = obj.getClass();
    try {
      Optional<Field> field = getAllFields(type).stream().filter(i -> i.getName().equals(name)).findFirst();
      if (!field.isPresent()) {
        return false;
      }
      Field f = field.get();
      f.setAccessible(true);
      Object objField = f.get(obj);
      Object otherFiled = f.get(other);
      return objField != null && objField.equals(otherFiled);
    } catch (SecurityException e) {
      return false;
    } catch (IllegalArgumentException e) {
      return false;
    } catch (IllegalAccessException e) {
      return false;
    }
  }
}
