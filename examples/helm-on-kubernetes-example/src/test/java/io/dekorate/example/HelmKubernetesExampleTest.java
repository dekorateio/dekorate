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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.dekorate.helm.model.Chart;
import io.dekorate.helm.model.ValuesSchema;
import io.dekorate.helm.model.ValuesSchemaProperty;
import io.dekorate.kubernetes.annotation.ServiceType;
import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;

class HelmKubernetesExampleTest {

  private static final String CHART_NAME = "myChart";
  private static final String CHART_OUTPUT_LOCATION = "META-INF/dekorate/helm/" + CHART_NAME;
  private static final String ROOT_CONFIG_NAME = "app";

  @Test
  public void shouldHelmManifestsBeGenerated() throws IOException {
    Chart chart = read("/Chart.yaml", Chart.class);
    assertNotNull(chart, "Chart is null!");
    assertEquals("v2", chart.getApiVersion());
    // Should be the same as in `dekorate.helm.chart` from properties.
    assertEquals(CHART_NAME, chart.getName());
    // Should contain expected dependencies
    assertEquals(3, chart.getDependencies().size());
    assertEquals("dependencyNameA", chart.getDependencies().get(0).getName());
    assertEquals("dependencyNameA", chart.getDependencies().get(0).getAlias());
    assertEquals("0.0.1", chart.getDependencies().get(0).getVersion());
    assertEquals("http://localhost:8080", chart.getDependencies().get(0).getRepository());
    assertEquals("dependencyNameB", chart.getDependencies().get(1).getName());
    assertEquals("app", chart.getDependencies().get(1).getAlias());
    assertEquals("dependencyNameC", chart.getDependencies().get(2).getName());
    assertEquals("app.database.enabled", chart.getDependencies().get(2).getCondition());
    assertEquals(2, chart.getDependencies().get(2).getTags().length);
    assertEquals("web", chart.getDependencies().get(2).getTags()[0]);
    assertEquals("frontend", chart.getDependencies().get(2).getTags()[1]);
    // Values.yaml manifest
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/values.yaml"));
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/values.dev.yaml"));
    // templates
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/templates/deployment.yaml"));
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/templates/ingress.yaml"));
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/templates/service.yaml"));
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/templates/_helpers.tpl"));
    // charts folder
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/charts"));
    // notes
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/templates/NOTES.txt"));
    // optional resources
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/LICENSE"));
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/README.md"));
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/values.schema.json"));
    // crds
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/crds"));
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(CHART_OUTPUT_LOCATION + "/crds/crontabs.stable.example.com.yaml"));
  }

  @Test
  public void chartsShouldContainExpectedData() throws IOException {
    Map<String, Object> chart = read("/Chart.yaml", Map.class);
    assertNotNull(chart, "Chart.yaml is null!");

    assertNotNull(chart.containsKey("annotations"), "Does not contain `annotations` from the user Charts.yml!");
    assertEquals(CHART_NAME, chart.get("name"), "The name was not replaced with the generated value!");
  }

  @Test
  public void valuesShouldContainExpectedData() throws IOException {
    Map<String, Object> values = read("/values.yaml", Map.class);
    assertNotNull(values, "Values is null!");

    assertNotNull(values.containsKey(ROOT_CONFIG_NAME), "Does not contain `" + ROOT_CONFIG_NAME + "`");
    assertNotNull(values.get(ROOT_CONFIG_NAME) instanceof Map, "Value `" + ROOT_CONFIG_NAME + "` is not a map!");

    // Rootless properties
    assertEquals("rootless-property", values.get("prop"));

    Map<String, Object> app = (Map<String, Object>) values.get(ROOT_CONFIG_NAME);
    // Should contain image
    assertNotNull(app.get("image"));
    // Should contain replicas
    assertEquals(3, app.get("replicas"));
    // Should contain service type
    assertEquals("NodePort", app.get("serviceType"));
    // Should NOT contain notFound: as this property is ignored
    assertNull(app.get("notFound"));
    // Should contain vcsUrl with the overridden value from properties
    assertEquals("Overridden", app.get("vcsUrl"));
    // Should include health check properties:
    // 1. tcp socket action
    Map<String, Object> livenessValues = (Map<String, Object>) app.get("livenessProbe");
    assertProbe(livenessValues, 11, 31);
    Map<String, Object> tcpSocketValues = (Map<String, Object>) livenessValues.get("tcpSocket");
    assertEquals("1111", tcpSocketValues.get("port"));
    assertEquals("my-service", tcpSocketValues.get("host"));
    // 2. http get action
    Map<String, Object> readinessValues = (Map<String, Object>) app.get("readinessProbe");
    assertProbe(readinessValues, 10, 30);
    Map<String, Object> httpGetValues = (Map<String, Object>) readinessValues.get("httpGet");
    assertEquals("/readiness", httpGetValues.get("path"));
    assertEquals(8080, httpGetValues.get("port"));
    assertEquals("HTTP", httpGetValues.get("scheme"));
    // 3. exec action
    Map<String, Object> startupValues = (Map<String, Object>) app.get("startupProbe");
    assertProbe(startupValues, 12, 32);
    Map<String, Object> execValues = (Map<String, Object>) startupValues.get("exec");
    List<String> command = (List<String>) execValues.get("command");
    assertEquals(2, command.size());
    assertEquals("command1", command.get(0));
    assertEquals("command2", command.get(1));
    // 4. helm expression
    assertEquals(readString("expected-ingress.yaml"), readString(CHART_OUTPUT_LOCATION + "/templates/ingress.yaml"));
    assertEquals(readString("expected-configmap.yaml"), readString(CHART_OUTPUT_LOCATION + "/templates/configmap.yaml"));
  }

  @Test
  public void valuesShouldContainExpectedDataInDevProfile() throws IOException {
    Map<String, Object> values = read("/values.dev.yaml", Map.class);
    assertNotNull(values, "Values is null!");

    assertNotNull(values.containsKey(ROOT_CONFIG_NAME), "Does not contain `" + ROOT_CONFIG_NAME + "`");
    assertNotNull(values.get(ROOT_CONFIG_NAME) instanceof Map, "Value `" + ROOT_CONFIG_NAME + "` is not a map!");
    Map<String, Object> helmExampleValues = (Map<String, Object>) values.get(ROOT_CONFIG_NAME);

    // Should contain image
    assertNotNull(helmExampleValues.get("image"));
    // Should contain replicas
    assertEquals(3, helmExampleValues.get("replicas"));
    // Should NOT contain notFound: as this property is ignored
    assertNull(helmExampleValues.get("notFound"));
    // Should contain vcsUrl with the value from properties
    assertEquals("Only for DEV!", helmExampleValues.get("vcsUrl"));
    // Should contain ingress with the value from properties
    assertEquals("my-test-host", helmExampleValues.get("host"));
  }

  @Test
  public void valuesFileShouldContainDependencyValues() throws IOException {
    ValuesSchema schema = read("/values.schema.json", ValuesSchema.class);
    // From properties
    assertEquals("My Values", schema.getTitle());
    // From the provided values schema json
    assertEquals(2, schema.getRequired().size());
    Iterator<String> requirements = schema.getRequired().iterator();
    assertEquals("protocol", requirements.next());
    assertEquals("port", requirements.next());
    ValuesSchemaProperty image = schema.getProperties().get("image");
    assertNotNull(image);
    assertEquals("Container Image", image.getDescription());
    assertEquals(2, image.getProperties().size());
    // From config references
    ValuesSchemaProperty app = schema.getProperties().get("app");
    assertNotNull(app);
    assertEquals(1, app.getRequired().size());
    assertEquals("serviceType", app.getRequired().iterator().next());
    ValuesSchemaProperty replicas = app.getProperties().get("replicas");
    assertNotNull(replicas);
    assertEquals(3, replicas.getMinimum());
    assertEquals(5, replicas.getMaximum());
    assertEquals("Overwrite default description!", replicas.getDescription());
    ValuesSchemaProperty serviceType = app.getProperties().get("serviceType");
    assertNotNull(serviceType);
    assertEquals("The service type to use.", serviceType.getDescription());
    assertEquals(ServiceType.values().length, serviceType.getEnumValues().size());
  }

  @Test
  public void validateValuesSchemaFile() throws IOException {
    Map<String, Object> values = read("/values.yaml", Map.class);
    Map<String, Object> dependencyA = (Map<String, Object>) values.get("dependencyNameA");
    assertEquals("aValue", dependencyA.get("config"));

    Map<String, Object> dependencyApp = (Map<String, Object>) values.get("app");
    Map<String, Object> config = (Map<String, Object>) dependencyApp.get("config");
    assertEquals("John", config.get("user"));
    assertEquals("mysql", config.get("database"));
  }

  private void assertProbe(Map<String, Object> probeValues, int expectedTimeoutSeconds, int expectedPeriodSeconds) {
    assertEquals(3, probeValues.get("failureThreshold"));
    assertEquals(expectedTimeoutSeconds, probeValues.get("timeoutSeconds"));
    assertEquals(expectedPeriodSeconds, probeValues.get("periodSeconds"));
    assertEquals(1, probeValues.get("successThreshold"));
    assertEquals(0, probeValues.get("initialDelaySeconds"));
  }

  private static <T> T read(String path, Class<T> clazz) throws IOException {
    return Serialization.yamlMapper().readValue(Main.class.getClassLoader()
      .getResourceAsStream(CHART_OUTPUT_LOCATION + path), clazz);
  }

  private static String readString(String path) {
    return new BufferedReader(
      new InputStreamReader(Main.class.getClassLoader().getResourceAsStream(path), StandardCharsets.UTF_8))
        .lines()
        .collect(Collectors.joining(System.lineSeparator()));
  }
}
