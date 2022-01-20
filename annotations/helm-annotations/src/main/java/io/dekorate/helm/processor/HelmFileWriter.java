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

import static io.dekorate.helm.config.HelmBuildConfigGenerator.HELM;
import static io.dekorate.helm.util.HelmTarArchiver.createTarBall;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import io.dekorate.ConfigReference;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Session;
import io.dekorate.WithConfigReferences;
import io.dekorate.helm.config.HelmChartConfig;
import io.dekorate.helm.model.Chart;
import io.dekorate.helm.model.HelmDependency;
import io.dekorate.helm.model.Maintainer;
import io.dekorate.processor.SimpleFileWriter;
import io.dekorate.project.Project;
import io.dekorate.utils.Serialization;
import io.dekorate.utils.Strings;

public class HelmFileWriter extends SimpleFileWriter {

  private static final String YAML = ".yaml";
  private static final String CHART_FILENAME = "Chart" + YAML;
  private static final String VALUES = "values";
  private static final String CHART_API_VERSION = "v1";
  private static final String TEMPLATES = "templates";
  private static final String CHARTS = "charts";
  private static final String NOTES = "NOTES.txt";
  private static final String KUBERNETES_CLASSIFIER = "helm";
  private static final String OPENSHIFT_CLASSIFIER = "helmshift";
  private static final String OPENSHIFT = "openshift";
  private static final String KIND = "kind";
  private static final String VALUES_START_TAG = "{{ .Values.";
  private static final String VALUES_END_TAG = " }}";
  private static final String EMPTY = "";
  private static final boolean APPEND = true;
  private static final Logger LOGGER = LoggerFactory.getLogger();

  public HelmFileWriter(Project project) {
    super(project);
  }

  @Override
  public Map<String, String> write(Session session) {
    Map<String, String> artifacts = super.write(session);
    session.getConfigurationRegistry().get(HelmChartConfig.class).ifPresent(helmConfig -> {
      if (helmConfig.isEnabled()) {
        List<ConfigReference> valuesReferences = getValuesReferences(helmConfig, session);

        try {
          LOGGER.info(String.format("Creating Helm Chart \"%s\"", helmConfig.getName()));
          Map<String, Object> prodValues = new HashMap<>();
          Map<String, Map<String, Object>> valuesByProfile = new HashMap<>();
          artifacts.putAll(processSourceFiles(helmConfig, valuesReferences, prodValues, valuesByProfile));
          artifacts.putAll(createChartYaml(helmConfig));
          artifacts.putAll(createValuesYaml(helmConfig, prodValues, valuesByProfile));
          artifacts.putAll(createTarball(helmConfig, artifacts, valuesByProfile.keySet()));
          // To follow Helm file structure standards:
          artifacts.putAll(createEmptyChartFolder(helmConfig));
          artifacts.putAll(addNotesIntoTemplatesFolder(helmConfig));

        } catch (IOException e) {
          throw new RuntimeException("Error writing resources", e);
        }
      }
    });

    return artifacts;
  }

  private Map<String, String> addNotesIntoTemplatesFolder(HelmChartConfig helmConfig) throws IOException {
    InputStream notesInputStream = HelmFileWriter.class.getResourceAsStream("/" + NOTES);
    Path outputDir = getChartOutputDir(helmConfig).resolve(TEMPLATES).resolve(NOTES);
    Files.copy(notesInputStream, outputDir);
    return Collections.singletonMap(outputDir.toString(), EMPTY);
  }

  private Map<String, String> createEmptyChartFolder(HelmChartConfig helmConfig) throws IOException {
    Path emptyChartsDir = getChartOutputDir(helmConfig).resolve(CHARTS);
    Files.createDirectories(emptyChartsDir);
    return Collections.singletonMap(emptyChartsDir.toString(), EMPTY);
  }

  private List<ConfigReference> getValuesReferences(HelmChartConfig helmBuildConfig, Session session) {
    List<ConfigReference> configReferences = new LinkedList<>();
    // From decorators
    for (WithConfigReferences decorator : session.getResourceRegistry().getConfigReferences()) {
      configReferences.addAll(decorator.getConfigReferences());
    }

    // From user
    Stream.of(helmBuildConfig.getValues()).forEach(valueReference -> configReferences
        .add(new ConfigReference(valueReference.getProperty(), valueReference.getJsonPaths(),
            valueReference.getValue().isEmpty() ? null : valueReference.getValue(), valueReference.getProfile())));
    return configReferences;
  }

  private Map<String, String> createValuesYaml(HelmChartConfig helmConfig, Map<String, Object> prodValues,
      Map<String, Map<String, Object>> valuesByProfile) throws IOException {

    Map<String, String> artifacts = new HashMap<>();

    // first, we process the values in each profile
    for (Map.Entry<String, Map<String, Object>> valuesInProfile : valuesByProfile.entrySet()) {
      String profile = valuesInProfile.getKey();
      Map<String, Object> values = valuesInProfile.getValue();
      // Populate the profiled values with the one from prod if the key does not exist
      for (Map.Entry<String, Object> prodValue : prodValues.entrySet()) {
        if (!values.containsKey(prodValue.getKey())) {
          values.put(prodValue.getKey(), prodValue.getValue());
        }
      }

      // Create the values.<profile>.yaml file
      artifacts.putAll(writeFileAsYaml(toMultiValueMap(values),
          getChartOutputDir(helmConfig).resolve(VALUES + "." + profile + YAML)));
    }

    // Next, we process the prod profile
    artifacts.putAll(writeFileAsYaml(toMultiValueMap(prodValues),
        getChartOutputDir(helmConfig).resolve(VALUES + YAML)));

    return artifacts;
  }

  private Map<String, String> createTarball(HelmChartConfig helmConfig, Map<String, String> artifacts, Set<String> profiles)
      throws IOException {

    File tarballFile = getOutputDir().resolve(String.format("%s-%s-%s.%s",
        helmConfig.getName(), getVersion(helmConfig), getHelmClassifier(artifacts), helmConfig.getExtension()))
        .toFile();

    LOGGER.debug(String.format("Creating Helm configuration Tarball: '%s'", tarballFile));

    Path helmSources = getChartOutputDir(helmConfig);

    List<File> yamls = new ArrayList<>();
    yamls.add(helmSources.resolve(CHART_FILENAME).toFile());
    yamls.add(helmSources.resolve(VALUES + YAML).toFile());
    for (String profile : profiles) {
      yamls.add(helmSources.resolve(VALUES + "." + profile + YAML).toFile());
    }

    yamls.addAll(listYamls(helmSources.resolve(TEMPLATES)));

    createTarBall(tarballFile, helmSources.toFile(), yamls, helmConfig.getExtension(),
        tae -> tae.setName(String.format("%s/%s", helmConfig.getName(), tae.getName())));

    return Collections.singletonMap(tarballFile.toString(), null);
  }

  private String getVersion(HelmChartConfig helmConfig) {
    if (helmConfig.getVersion() == null || helmConfig.getVersion().isEmpty()) {
      return getProject().getBuildInfo().getVersion();
    }

    return helmConfig.getVersion();
  }

  private Map<String, String> processSourceFiles(HelmChartConfig helmConfig, List<ConfigReference> valuesReferences,
      Map<String, Object> prodValues, Map<String, Map<String, Object>> valuesByProfile) throws IOException {

    Path templatesDir = getChartOutputDir(helmConfig).resolve(TEMPLATES);
    Files.createDirectories(templatesDir);
    List<String> yamlsContent = replaceValuesInYamls(valuesReferences, prodValues, valuesByProfile);
    // Split yamls in separated files by kind
    for (String yamlContent : yamlsContent) {
      List<Map<Object, Object>> resources = Serialization.unmarshalAsListOfMaps(yamlContent);
      for (Map<Object, Object> resource : resources) {
        String kind = (String) resource.get(KIND);
        Path targetFile = templatesDir.resolve(kind.toLowerCase() + YAML);

        // Adapt the values tag to Helm standards:
        String adaptedString = Serialization.yamlMapper().writeValueAsString(resource)
            .replaceAll(Pattern.quote("\"" + VALUES_START_TAG), VALUES_START_TAG)
            .replaceAll(Pattern.quote(VALUES_END_TAG + "\""), VALUES_END_TAG);

        writeFile(adaptedString, targetFile);
      }
    }

    return Collections.emptyMap();
  }

  private List<String> replaceValuesInYamls(List<ConfigReference> valuesReferences, Map<String, Object> prodValues,
      Map<String, Map<String, Object>> valuesByProfile) throws IOException {

    List<String> yamlsContent = new LinkedList<>();
    for (File file : listYamls(getOutputDir())) {
      // Read yaml
      List<Map<Object, Object>> yaml = Serialization.unmarshalAsListOfMaps(file.toPath());

      // Parse to json in order to process jsonpaths
      String json = Serialization.jsonMapper().writeValueAsString(yaml);
      for (ConfigReference valueReference : valuesReferences) {
        String valueReferenceProperty = Strings.kebabToCamelCase(valueReference.getProperty());

        DocumentContext jsonContext = JsonPath.parse(json);

        // Check whether path exists
        Object currentValue = null;
        for (String jsonPath : valueReference.getJsonPaths()) {
          try {
            currentValue = jsonContext.read(jsonPath, Object.class);
          } catch (PathNotFoundException ex) {
            LOGGER
                .warning(String.format("Property '%s' is ignored in Helm generation because the json Path '%s' was not found. ",
                    valueReferenceProperty, jsonPath));
            continue;
          }

          json = jsonContext
              .set(jsonPath, VALUES_START_TAG + valueReferenceProperty + VALUES_END_TAG)
              .jsonString();

          Object value = valueReference.getValue();
          if (value == null) {
            if (currentValue instanceof List && !((List) currentValue).isEmpty()) {
              // We get the first value
              value = ((List) currentValue).get(0);
            } else {
              value = currentValue;
            }
          }

          String valueProfile = valueReference.getProfile();
          Map<String, Object> values = prodValues;
          if (Strings.isNotNullOrEmpty(valueProfile)) {
            values = valuesByProfile.get(valueProfile);
            if (values == null) {
              values = new HashMap<>();
              valuesByProfile.put(valueProfile, values);
            }
          }

          values.put(valueReferenceProperty, value);
        }
      }

      // Parse back to yaml and write to file
      StringBuilder sb = new StringBuilder();
      JsonNode jsonTree = Serialization.jsonMapper().readTree(json);
      for (JsonNode jsonElement : jsonTree) {
        sb.append(Serialization.yamlMapper().writeValueAsString(jsonElement));
      }

      yamlsContent.add(sb.toString());
    }

    return yamlsContent;
  }

  private Map<String, String> createChartYaml(HelmChartConfig helmConfig) throws IOException {
    final Chart chart = new Chart();
    chart.setApiVersion(CHART_API_VERSION);
    chart.setName(helmConfig.getName());
    chart.setVersion(getVersion(helmConfig));
    chart.setDescription(helmConfig.getDescription());
    chart.setHome(helmConfig.getHome());
    chart.setSources(Arrays.asList(helmConfig.getSources()));
    chart.setMaintainers(Arrays.stream(helmConfig.getMaintainers())
        .map(m -> new Maintainer(m.getName(), m.getEmail(), m.getUrl()))
        .collect(Collectors.toList()));
    chart.setIcon(helmConfig.getIcon());
    chart.setKeywords(Arrays.asList(helmConfig.getKeywords()));
    chart.setDependencies(Arrays.stream(helmConfig.getDependencies())
        .map(d -> new HelmDependency(d.getName(), d.getVersion(), d.getRepository()))
        .collect(Collectors.toList()));

    Path yml = getChartOutputDir(helmConfig).resolve(CHART_FILENAME).normalize();
    return writeFileAsYaml(chart, yml);
  }

  private Map<String, String> writeFileAsYaml(Object data, Path file) throws IOException {
    String value = Serialization.asYaml(data);
    return writeFile(value, file);
  }

  private Map<String, String> writeFile(String value, Path file) throws IOException {
    try (FileWriter writer = new FileWriter(file.toFile(), APPEND)) {
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

  private Path getChartOutputDir(HelmChartConfig helmConfig) {
    return getOutputDir().resolve(HELM).resolve(helmConfig.getName());
  }

  private static List<File> listYamls(Path directory) {
    return Stream.of(Optional.ofNullable(directory.toFile().listFiles()).orElse(new File[0]))
        .filter(File::isFile)
        .filter(f -> f.getName().toLowerCase().matches(".*?\\.ya?ml$"))
        .collect(Collectors.toList());
  }

  private static Map<String, Object> toMultiValueMap(Map<String, Object> map) {
    Map<String, Object> multiValueMap = new HashMap<>();
    map.forEach((k, v) -> {

      String[] nodes = k.split(Pattern.quote("."));
      if (nodes.length == 1) {
        multiValueMap.put(k, v);
      } else {
        Map<String, Object> auxKeyValue = multiValueMap;
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

    return multiValueMap;
  }
}
