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
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.dekorate.helm.model.Chart;
import io.dekorate.utils.Serialization;

class HelmKubernetesExampleTest {

  private static final String CHART_NAME = "myChart";
  private static final String CHART_OUTPUT_LOCATION = "META-INF/dekorate/helm/" + CHART_NAME;
  private static final String ROOT_CONFIG_NAME = "helmOnKubernetesExample";

  @Test
  public void shouldHelmManifestsBeGenerated() throws IOException {
    Chart chart = Serialization.yamlMapper().readValue(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/Chart.yaml"), Chart.class);
    assertNotNull(chart, "Chart is null!");
    // Should be the same as in `dekorate.helm.chart` from properties.
    assertEquals(CHART_NAME, chart.getName());
    // Should contain expected dependencies
    assertEquals(2, chart.getDependencies().size());
    assertEquals("dependency-name-a", chart.getDependencies().get(0).getName());
    assertEquals("dependency-name-a", chart.getDependencies().get(0).getAlias());
    assertEquals("0.0.1", chart.getDependencies().get(0).getVersion());
    assertEquals("http://localhost:8080", chart.getDependencies().get(0).getRepository());
    assertEquals("dependency-name-b", chart.getDependencies().get(1).getName());
    assertEquals("app", chart.getDependencies().get(1).getAlias());
    // Values.yaml manifest
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/values.yaml"));
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/values.dev.yaml"));
    // templates
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/templates/deployment.yaml"));
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/templates/ingress.yaml"));
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/templates/service.yaml"));
    // empty charts folder
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/charts"));
    // notes
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/templates/NOTES.txt"));
    // zip manifest
    String zipName = String.format("META-INF/dekorate/helm/%s-%s-helm.tar.gz", chart.getName(), chart.getVersion());
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(zipName), "File '" + zipName + "' not found!");
  }

  @Test
  public void valuesShouldContainExpectedData() throws IOException {
    Map<String, Object> values = Serialization.yamlMapper().readValue(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/values.yaml"), Map.class);
    assertNotNull(values, "Values is null!");

    assertNotNull(values.containsKey(ROOT_CONFIG_NAME), "Does not contain `" + ROOT_CONFIG_NAME + "`");
    assertNotNull(values.get(ROOT_CONFIG_NAME) instanceof Map, "Value `" + ROOT_CONFIG_NAME + "` is not a map!");
    Map<String, Object> helmExampleValues = (Map<String, Object>) values.get(ROOT_CONFIG_NAME);

    // Should contain image
    assertNotNull(helmExampleValues.get("image"));
    // Should contain replicas
    assertEquals(3, helmExampleValues.get("replicas"));
    // Should NOT contain not-found: as this property is ignored
    assertNull(helmExampleValues.get("not-found"));
    // Should contain vcs-url with the overridden value from properties
    assertEquals("Overridden", helmExampleValues.get("vcsUrl"));
    // Should include health check properties:
    // 1. tcp socket action
    Map<String, Object> livenessValues = (Map<String, Object>) helmExampleValues.get("livenessProbe");
    assertProbe(livenessValues, 11, 31);
    Map<String, Object> tcpSocketValues = (Map<String, Object>) livenessValues.get("tcpSocket");
    assertEquals("1111", tcpSocketValues.get("port"));
    assertEquals("my-service", tcpSocketValues.get("host"));
    // 2. http get action
    Map<String, Object> readinessValues = (Map<String, Object>) helmExampleValues.get("readinessProbe");
    assertProbe(readinessValues, 10, 30);
    Map<String, Object> httpGetValues = (Map<String, Object>) readinessValues.get("httpGet");
    assertEquals("/readiness", httpGetValues.get("path"));
    // 3. exec action
    Map<String, Object> startupValues = (Map<String, Object>) helmExampleValues.get("startupProbe");
    assertProbe(startupValues, 12, 32);
    Map<String, Object> execValues = (Map<String, Object>) startupValues.get("exec");
    List<String> command = (List<String>) execValues.get("command");
    assertEquals(2, command.size());
    assertEquals("command1", command.get(0));
    assertEquals("command2", command.get(1));
  }

  @Test
  public void valuesShouldContainExpectedDataInDevProfile() throws IOException {
    Map<String, Object> values = Serialization.yamlMapper().readValue(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/values.dev.yaml"), Map.class);
    assertNotNull(values, "Values is null!");

    assertNotNull(values.containsKey(ROOT_CONFIG_NAME), "Does not contain `" + ROOT_CONFIG_NAME + "`");
    assertNotNull(values.get(ROOT_CONFIG_NAME) instanceof Map, "Value `" + ROOT_CONFIG_NAME + "` is not a map!");
    Map<String, Object> helmExampleValues = (Map<String, Object>) values.get(ROOT_CONFIG_NAME);

    // Should contain image
    assertNotNull(helmExampleValues.get("image"));
    // Should contain replicas
    assertEquals(3, helmExampleValues.get("replicas"));
    // Should NOT contain not-found: as this property is ignored
    assertNull(helmExampleValues.get("not-found"));
    // Should contain vcs-url with the value from properties
    assertEquals("Only for DEV!", helmExampleValues.get("vcsUrl"));
    // Should contain ingress with the value from properties
    assertEquals("my-test-host", helmExampleValues.get("host"));
  }

  private void assertProbe(Map<String, Object> probeValues, int expectedTimeoutSeconds, int expectedPeriodSeconds) {
    assertEquals(3, probeValues.get("failureThreshold"));
    assertEquals(expectedTimeoutSeconds, probeValues.get("timeoutSeconds"));
    assertEquals(expectedPeriodSeconds, probeValues.get("periodSeconds"));
    assertEquals(1, probeValues.get("successThreshold"));
    assertEquals(0, probeValues.get("initialDelaySeconds"));
  }
}
