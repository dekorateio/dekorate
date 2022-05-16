package io.dekorate.helm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;

public class HelmExpressionParserTest {

  private static final File TEST_FILE = new File(HelmExpressionParserTest.class.getResource("/test-kubernetes.yml").getFile());

  private List<Map<Object, Object>> resources;
  private HelmExpressionParser parser;

  @BeforeEach
  public void setup() throws IOException {
    resources = Serialization.unmarshalAsListOfMaps(TEST_FILE.toPath());
    parser = new HelmExpressionParser(resources);
  }

  @Test
  public void parseSimpleExpression() throws IOException {
    Object found = parser.readAndSet("metadata.name", "{{ .Values.app.name }}");
    assertEquals("example", found);
    assertGeneratedYaml("parseSimpleExpression");
  }

  @Test
  public void parseExpressionWithEscape() throws IOException {
    Object found = parser.readAndSet("spec.selector.matchLabels.'app.kubernetes.io/name'", "{{ .Values.app.label }}");
    assertEquals("example", found);
    assertGeneratedYaml("parseExpressionWithEscape");
  }

  @Test
  public void parseArrayExpression() throws IOException {
    Object found = parser.readAndSet("spec.ports.port", "{{ .Values.app.port }}");
    assertEquals(80, found);
    assertGeneratedYaml("parseArrayExpression");
  }

  @Test
  public void parseExpressionWithEqual() throws IOException {
    Object found = parser.readAndSet("(kind == Deployment).metadata.name", "{{ .Values.app.name }}");
    assertEquals("example", found);
    assertGeneratedYaml("parseExpressionWithEqual");
  }

  @Test
  public void parseExpressionWithTwoEqualOperationsAndNotFound() throws IOException {
    Object found = parser.readAndSet("(kind == Deployment && metadata.name == notFound).metadata.name",
        "{{ .Values.app.name }}");
    assertNull(found);
    assertGeneratedYaml("no-changes");
  }

  @Test
  public void parseExpressionWithTwoEqualOperationsAndFound() throws IOException {
    Object found = parser.readAndSet("(kind == Deployment && metadata.name == example).metadata.name",
        "{{ .Values.app.name }}");
    assertEquals("example", found);
    assertGeneratedYaml("parseExpressionWithEqual");
  }

  private void assertGeneratedYaml(String method) throws IOException {
    String actual = Serialization.yamlMapper().writeValueAsString(resources);
    String expected = new String(
        IOUtils.toByteArray(getClass().getResourceAsStream("/expected-" + method + "-kubernetes.yml")));
    assertEquals(expected, actual, "Unexpected generated YAML file. Found: " + actual);
  }
}
