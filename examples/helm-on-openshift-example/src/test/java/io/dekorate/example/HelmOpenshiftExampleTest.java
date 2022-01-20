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

package io.dekorate.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.dekorate.helm.model.Chart;
import io.dekorate.utils.Serialization;

class HelmOpenshiftExampleTest {

  private static final String CHART_NAME = "myOcpChart";
  private static final String CHART_OUTPUT_LOCATION = "META-INF/dekorate/" + CHART_NAME;
  private static final String ROOT_CONFIG_NAME = "helmOnOpenshiftExample";

  @Test
  public void shouldHelmManifestsBeGenerated() throws IOException {
    Chart chart = Serialization.yamlMapper().readValue(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/Chart.yaml"), Chart.class);
    assertNotNull(chart, "Chart is null!");
    // Should be the same as in `dekorate.helm.chart` from properties.
    assertEquals(CHART_NAME, chart.getName());
    // Values.yaml manifest
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/values.yaml"));
    // templates
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/templates/openshift.yml"));
    // zip manifest
    String zipName = String.format("META-INF/dekorate/%s-%s-helmshift.tar.gz", chart.getName(), chart.getVersion());
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(zipName), "File '" + zipName + "' not found!");
  }

  @Test
  public void valuesShouldContainExpectedData() throws IOException {
    Map<String, Object> values = Serialization.yamlMapper().readValue(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/values.yaml"), Map.class);
    assertNotNull(values, "Values is null!");

    assertNotNull(values.containsKey(ROOT_CONFIG_NAME), "Does not contain `" + ROOT_CONFIG_NAME + "`");
    assertNotNull(values.get(ROOT_CONFIG_NAME) instanceof Map, "Value `" + ROOT_CONFIG_NAME + "` is not a map!");
    Map<String, Object> helmExampleValues = (Map<String, Object>) values.get(ROOT_CONFIG_NAME);

    // Should contain s2i configuration
    assertNotNull(helmExampleValues.get("s2iJava"));
    assertEquals("fabric8/s2i-java", ((Map<String, Object>) helmExampleValues.get("s2iJava")).get("builderImage"));
    // Should contain replicas
    assertEquals(3, helmExampleValues.get("replicas"));
    // Should NOT contain not-found: as this property is ignored
    assertNull(helmExampleValues.get("not-found"));
    // Should contain commit-id
    assertNotNull(helmExampleValues.get("commitId"));
    // Shoult contain vcs-url with the overridden value from properties
    assertEquals("Overridden", helmExampleValues.get("vcsUrl"));
  }
}
