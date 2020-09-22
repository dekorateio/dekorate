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

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class MapsTest {

  @Test
  public void testMapFromProperties() throws Exception {
    try (InputStream is = MapsTest.class.getClassLoader().getResourceAsStream("simple.properties")) {
      Map<String, Object> map = Maps.fromProperties(is);
      checkFlattenMap(map);
    }
  }

  @Test
  public void testMapFromPropertiesWithArrays() throws Exception {
    try (InputStream is = MapsTest.class.getClassLoader().getResourceAsStream("kebab.properties")) {
      Map<String, Object> map = Maps.fromProperties(is);
      Map<String, Object> result = Maps.kebabToCamelCase(map);
      Map<String, Object> kubernetes = (Map<String, Object>) result.get("kubernetes");
      Map<String, Object>[] envVars = (Map<String, Object>[]) kubernetes.get("envVars");
      assertEquals("FOO", envVars[0].get("name"));
      assertEquals("BAR", envVars[0].get("value"));
    }
  }

  @Test
  public void testMapFromYAML() throws Exception {
    try (InputStream is = MapsTest.class.getClassLoader().getResourceAsStream("simple.yml")) {
      Map<String, Object> map = Maps.fromYaml(is);
      checkFlattenMap(map);
    }
  }

  @Test
  public void testKebabToCamelCase() throws Exception {
    try (InputStream is = MapsTest.class.getClassLoader().getResourceAsStream("kebab.yml")) {
      Map<String, Object> map = Maps.fromYaml(is);
      Map<String, Object> result = Maps.kebabToCamelCase(map);
      Map<String, Object> kubernetes = (Map<String, Object>) result.get("kubernetes");
      Map<String, Object> readinesProbe = (Map<String, Object>) kubernetes.get("readinesProbe");
      List<Map<String, Object>> envVars = (List<Map<String, Object>>) kubernetes.get("envVars");
      System.out.println(envVars);
      assertEquals("KEY1", envVars.get(0).get("name"));
      assertEquals("VALUE1", envVars.get(0).get("value"));
      assertEquals("KEY2", envVars.get(1).get("name"));
      assertEquals("VALUE2", envVars.get(1).get("value"));
      assertEquals(10, readinesProbe.get("periodSeconds"));
    }
  }

  private void checkFlattenMap(Map<String, Object> map) {
    assertNotNull(map);
    Map kubernetes = (Map) map.get("kubernetes");
    assertNotNull(kubernetes);
    assertTrue(kubernetes.containsKey("group"));
    assertTrue(kubernetes.containsKey("name"));
    assertTrue(kubernetes.containsKey("version"));
    assertTrue(kubernetes.containsKey("labels"));
    Object labels = kubernetes.get("labels");
    assertTrue(labels instanceof Map);
    assertEquals("bar", ((Map) labels).get("foo"));

    Map openshift = (Map) map.get("openshift");
    assertNotNull(openshift);
    assertEquals("boo", openshift.get("name"));
    labels = openshift.get("labels");
    assertTrue(labels instanceof Map);
    assertEquals("bar", ((Map) labels).get("foo"));
  }

  @Test
  public void onlyPrefixShouldFailMapFromProperties() throws Exception {
    try (InputStream is = MapsTest.class.getClassLoader().getResourceAsStream("wrong.properties")) {
      try {
        Maps.fromProperties(is);
        fail();
      } catch (IllegalArgumentException expected) {
      }
    }
  }
}
