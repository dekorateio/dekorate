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

package io.dekorate.helm.processor;

import static io.dekorate.helm.util.HelmTarArchiver.createTarBall;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Session;
import io.dekorate.WithConfigReference;
import io.dekorate.helm.config.HelmBuildConfig;
import io.dekorate.helm.model.Chart;
import io.dekorate.helm.model.HelmDependency;
import io.dekorate.helm.model.Maintainer;
import io.dekorate.helm.model.Value;
import io.dekorate.processor.SimpleFileWriter;
import io.dekorate.project.Project;
import io.dekorate.utils.Serialization;

public class HelmFileWriter extends SimpleFileWriter {

  private static final String CHART_FILENAME = "Chart.yaml";
  private static final String VALUES_FILENAME = "values.yaml";
  private static final String CHART_API_VERSION = "v1";
  private static final String TEMPLATES = "templates";
  private static final String KUBERNETES_CLASSIFIER = "helm";
  private static final String OPENSHIFT_CLASSIFIER = "helmshift";
  private static final String OPENSHIFT = "openshift";
  private static final Logger LOGGER = LoggerFactory.getLogger();

  public HelmFileWriter(Project project) {
    super(project);
  }

  @Override
  public Map<String, String> write(Session session) {
    Map<String, String> artifacts = super.write(session);
    session.getConfigurationRegistry().get(HelmBuildConfig.class).ifPresent(helmConfig -> {
      if (helmConfig.isEnabled()) {
        List<Value> valuesReferences = getValuesReferences(helmConfig, session);

        try {
          LOGGER.info(String.format("Creating Helm Chart \"%s\"", helmConfig.getChart()));
          Map<String, Object> values = new HashMap<>();
          artifacts.putAll(processSourceFiles(valuesReferences, values));
          artifacts.putAll(createChartYaml(helmConfig));
          artifacts.putAll(createValuesYaml(values));
          artifacts.putAll(createTarball(helmConfig, artifacts));

        } catch (IOException e) {
          throw new RuntimeException("Error writing resources", e);
        }
      }
    });

    return artifacts;
  }

  private List<Value> getValuesReferences(HelmBuildConfig helmBuildConfig, Session session) {
    List<Value> configReferences = new ArrayList<>();
    // From decorators
    for (Map.Entry<String, WithConfigReference> entry : session.getResourceRegistry().getConfigReferences().entrySet()) {
      configReferences.add(new Value(entry.getValue().getConfigReference(),
          entry.getValue().getJsonPathProperty(), entry.getValue().getConfigValue()));
    }

    // From user
    Stream.of(helmBuildConfig.getValues()).forEach(valueReference -> configReferences
        .add(new Value(valueReference.getProperty(), valueReference.getJsonPath(),
            valueReference.getValue().isEmpty() ? null : valueReference.getValue())));
    return configReferences;
  }

  private Map<String, String> createValuesYaml(Map<String, Object> values) throws IOException {
    Map<String, Object> keyValue = new HashMap<>();
    values.forEach((k, v) -> {

      String[] nodes = k.split(Pattern.quote("."));
      if (nodes.length == 1) {
        keyValue.put(k, v);
      } else {
        Map<String, Object> auxKeyValue = keyValue;
        for (int index = 0; index < nodes.length - 1; index++) {
          String nodeName = nodes[index];
          Object nodeKeyValue = auxKeyValue.get(nodeName);
          if (nodeKeyValue == null || !(nodeKeyValue instanceof Map)) {
            nodeKeyValue = new HashMap<>();
          }

          auxKeyValue.put(nodes[index], nodeKeyValue);
          auxKeyValue = (Map<String, Object>) nodeKeyValue;
        }

        auxKeyValue.put(nodes[nodes.length - 1], v);
      }
    });

    Path valuesFile = getOutputDir().resolve(VALUES_FILENAME);
    return writeFileAsYaml(keyValue, valuesFile);
  }

  private Map<String, String> createTarball(HelmBuildConfig helmConfig, Map<String, String> artifacts) throws IOException {

    File tarballFile = getOutputDir().resolve(String.format("%s-%s-%s.%s",
        helmConfig.getChart(), getVersion(helmConfig), getHelmClassifier(artifacts), helmConfig.getChartExtension()))
        .toFile();

    LOGGER.debug(String.format("Creating Helm configuration Tarball: '%s'", tarballFile));

    List<File> yamls = new ArrayList<>();
    yamls.add(getOutputDir().resolve(CHART_FILENAME).toFile());
    yamls.add(getOutputDir().resolve(VALUES_FILENAME).toFile());
    yamls.addAll(listYamls(getOutputDir().resolve(TEMPLATES)));

    createTarBall(tarballFile, getOutputDir().toFile(), yamls, helmConfig.getChartExtension(),
        tae -> tae.setName(String.format("%s/%s", helmConfig.getChart(), tae.getName())));

    return Collections.singletonMap(tarballFile.toString(), null);
  }

  private String getVersion(HelmBuildConfig helmConfig) {
    if (helmConfig.getVersion() == null || helmConfig.getVersion().isEmpty()) {
      return getProject().getBuildInfo().getVersion();
    }

    return helmConfig.getVersion();
  }

  private Map<String, String> processSourceFiles(List<Value> valuesReferences, Map<String, Object> values)
      throws IOException {
    Map<String, String> sourceFiles = new HashMap<>();

    Path templatesDir = getOutputDir().resolve(TEMPLATES);
    Files.createDirectory(templatesDir);
    for (File file : listYamls(getOutputDir())) {
      // Read yaml
      List<Map<Object, Object>> yaml = Serialization.unmarshalAsListOfMaps(file.toPath());

      // Parse to json in order to process jsonpaths
      String json = Serialization.jsonMapper().writeValueAsString(yaml);
      for (Value valueReference : valuesReferences) {
        DocumentContext jsonContext = JsonPath.parse(json);

        // Check whether path exists
        Object currentValue;
        try {
          currentValue = jsonContext.read(valueReference.getJsonPath(), Object.class);
        } catch (PathNotFoundException ex) {
          LOGGER.warning(String.format("Property '%s' is ignored in Helm generation because the json Path '%s' was not found. ",
              valueReference.getProperty(), valueReference.getJsonPath()));
          continue;
        }

        if (valueReference.getValue() == null) {
          if (currentValue instanceof List && !((List) currentValue).isEmpty()) {
            // We get the first value
            values.put(valueReference.getProperty(), ((List) currentValue).get(0));
          } else {
            values.put(valueReference.getProperty(), currentValue);
          }
        } else {
          values.put(valueReference.getProperty(), valueReference.getValue());
        }

        json = jsonContext
            .set(valueReference.getJsonPath(), "{{ .Values." + valueReference.getProperty() + " }}")
            .jsonString();
      }

      Path targetFile = templatesDir.resolve(file.getName());

      // Parse back to yaml and write to file
      StringBuilder sb = new StringBuilder();
      JsonNode jsonTree = Serialization.jsonMapper().readTree(json);
      for (JsonNode jsonElement : jsonTree) {
        sb.append(Serialization.yamlMapper().writeValueAsString(jsonElement));
      }

      writeFile(sb.toString(), targetFile);
    }

    return sourceFiles;
  }

  private Map<String, String> createChartYaml(HelmBuildConfig helmConfig) throws IOException {
    final Chart chart = new Chart();
    chart.setApiVersion(CHART_API_VERSION);
    chart.setName(helmConfig.getChart());
    chart.setVersion(getVersion(helmConfig));
    chart.setDescription(helmConfig.getDescription());
    chart.setHome(helmConfig.getHome());
    chart.setSources(Arrays.asList(helmConfig.getSources()));
    chart.setMaintainers(Arrays.stream(helmConfig.getMaintainers())
        .map(m -> new Maintainer(m.getName(), m.getEmail()))
        .collect(Collectors.toList()));
    chart.setIcon(helmConfig.getIcon());
    chart.setKeywords(Arrays.asList(helmConfig.getKeywords()));
    chart.setEngine(helmConfig.getEngine());
    chart.setDependencies(Arrays.stream(helmConfig.getDependencies())
        .map(d -> new HelmDependency(d.getName(), d.getVersion(), d.getRepository()))
        .collect(Collectors.toList()));

    Path yml = getOutputDir().resolve(CHART_FILENAME).normalize();
    return writeFileAsYaml(chart, yml);
  }

  private Map<String, String> writeFileAsYaml(Object data, Path file) throws IOException {
    String value = Serialization.asYaml(data);
    return writeFile(value, file);
  }

  private Map<String, String> writeFile(String value, Path file) throws IOException {
    try (FileWriter writer = new FileWriter(file.toFile())) {
      writer.write(value);
      return Collections.singletonMap(file.toString(), value);
    }
  }

  private String getHelmClassifier(Map<String, String> artifacts) {
    if (artifacts.keySet().stream().anyMatch(a -> a.contains(OPENSHIFT))) {
      return OPENSHIFT_CLASSIFIER;
    }

    return KUBERNETES_CLASSIFIER;
  }

  private static List<File> listYamls(Path directory) {
    return Stream.of(Optional.ofNullable(directory.toFile().listFiles()).orElse(new File[0]))
        .filter(File::isFile)
        .filter(f -> f.getName().toLowerCase().matches(".*?\\.ya?ml$"))
        .collect(Collectors.toList());
  }
}
