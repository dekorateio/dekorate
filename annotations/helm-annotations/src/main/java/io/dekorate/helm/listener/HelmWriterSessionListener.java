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
package io.dekorate.helm.listener;

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
import java.util.Collection;
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

import io.dekorate.ConfigReference;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Session;
import io.dekorate.SessionListener;
import io.dekorate.WithConfigReferences;
import io.dekorate.WithProject;
import io.dekorate.WithSession;
import io.dekorate.helm.config.HelmChartConfig;
import io.dekorate.helm.config.ValueReference;
import io.dekorate.helm.model.Chart;
import io.dekorate.helm.model.HelmDependency;
import io.dekorate.helm.model.Maintainer;
import io.dekorate.helm.util.HelmExpressionParser;
import io.dekorate.project.Project;
import io.dekorate.utils.Serialization;
import io.dekorate.utils.Strings;

public class HelmWriterSessionListener implements SessionListener, WithProject, WithSession {

  private static final String YAML = ".yaml";
  private static final String YAML_REG_EXP = ".*?\\.ya?ml$";
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

  /**
   * Invoked when the session is closed
   */
  @Override
  public void onClosed() {
    Session session = getSession();
    Project project = getProject();
    Path outputDir = project.getBuildInfo().getClassOutputDir().resolve(project.getDekorateOutputDir());
    session.getConfigurationRegistry().get(HelmChartConfig.class).ifPresent(
        helmConfig -> writeHelmFiles(session, project, helmConfig, outputDir, listYamls(outputDir)));
  }

  /**
   * Needs to be public in order to be called from outside the session context.
   * 
   * @return the list of the Helm generated files.
   */
  public Map<String, String> writeHelmFiles(Session session, Project project, HelmChartConfig helmConfig, Path outputDir,
      Collection<File> generatedFiles) {
    Map<String, String> artifacts = new HashMap<>();
    if (helmConfig.isEnabled()) {
      validateHelmConfig(helmConfig);

      List<ConfigReference> valuesReferences = getValuesReferences(helmConfig, session);

      try {
        LOGGER.info(String.format("Creating Helm Chart \"%s\"", helmConfig.getName()));
        Map<String, Object> prodValues = new HashMap<>();
        Map<String, Map<String, Object>> valuesByProfile = new HashMap<>();
        artifacts.putAll(processSourceFiles(helmConfig, outputDir, generatedFiles, valuesReferences, prodValues,
            valuesByProfile));
        artifacts.putAll(createChartYaml(helmConfig, project, outputDir));
        artifacts.putAll(createValuesYaml(helmConfig, outputDir, prodValues, valuesByProfile));
        if (helmConfig.isCreateTarFile()) {
          artifacts.putAll(createTarball(helmConfig, project, outputDir, artifacts, valuesByProfile.keySet()));
        }

        // To follow Helm file structure standards:
        artifacts.putAll(createEmptyChartFolder(helmConfig, outputDir));
        artifacts.putAll(addNotesIntoTemplatesFolder(helmConfig, outputDir));

      } catch (IOException e) {
        throw new RuntimeException("Error writing resources", e);
      }
    }

    return artifacts;
  }

  private void validateHelmConfig(HelmChartConfig helmConfig) {
    if (Strings.isNullOrEmpty(helmConfig.getName())) {
      throw new RuntimeException("Helm Chart name is required!");
    }
  }

  private Map<String, String> addNotesIntoTemplatesFolder(HelmChartConfig helmConfig, Path outputDir) throws IOException {
    InputStream notesInputStream = HelmWriterSessionListener.class.getResourceAsStream(helmConfig.getNotes());
    Path chartOutputDir = getChartOutputDir(helmConfig, outputDir).resolve(TEMPLATES).resolve(NOTES);
    Files.copy(notesInputStream, chartOutputDir);
    return Collections.singletonMap(chartOutputDir.toString(), EMPTY);
  }

  private Map<String, String> createEmptyChartFolder(HelmChartConfig helmConfig, Path outputDir) throws IOException {
    Path emptyChartsDir = getChartOutputDir(helmConfig, outputDir).resolve(CHARTS);
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
    Stream.of(helmBuildConfig.getValues()).map(this::toConfigReference).forEach(configReferences::add);
    return configReferences;
  }

  private ConfigReference toConfigReference(ValueReference valueReference) {
    return new ConfigReference(valueReference.getProperty(),
        valueReference.getPaths(),
        Strings.isNullOrEmpty(valueReference.getValue()) ? null : valueReference.getValue(), valueReference.getProfile());
  }

  private Map<String, String> createValuesYaml(HelmChartConfig helmConfig, Path outputDir, Map<String, Object> prodValues,
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
          getChartOutputDir(helmConfig, outputDir).resolve(VALUES + "." + profile + YAML)));
    }

    // Next, we process the prod profile
    artifacts.putAll(writeFileAsYaml(toMultiValueMap(prodValues),
        getChartOutputDir(helmConfig, outputDir).resolve(VALUES + YAML)));

    return artifacts;
  }

  private Map<String, String> createTarball(HelmChartConfig helmConfig, Project project, Path outputDir,
      Map<String, String> artifacts, Set<String> profiles) throws IOException {

    File tarballFile = outputDir.resolve(HELM).resolve(String.format("%s-%s-%s.%s",
        helmConfig.getName(), getVersion(helmConfig, project), getHelmClassifier(artifacts), helmConfig.getExtension()))
        .toFile();

    LOGGER.debug(String.format("Creating Helm configuration Tarball: '%s'", tarballFile));

    Path helmSources = getChartOutputDir(helmConfig, outputDir);

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

  private String getVersion(HelmChartConfig helmConfig, Project project) {
    if (Strings.isNullOrEmpty(helmConfig.getVersion())) {
      return project.getBuildInfo().getVersion();
    }

    return helmConfig.getVersion();
  }

  private Map<String, String> processSourceFiles(HelmChartConfig helmConfig, Path outputDir, Collection<File> generatedFiles,
      List<ConfigReference> valuesReferences, Map<String, Object> prodValues,
      Map<String, Map<String, Object>> valuesByProfile) throws IOException {

    Path templatesDir = getChartOutputDir(helmConfig, outputDir).resolve(TEMPLATES);
    Files.createDirectories(templatesDir);
    List<Map<Object, Object>> resources = replaceValuesInYamls(generatedFiles, valuesReferences, prodValues, valuesByProfile);
    // Split yamls in separated files by kind
    for (Map<Object, Object> resource : resources) {
      String kind = (String) resource.get(KIND);
      Path targetFile = templatesDir.resolve(kind.toLowerCase() + YAML);

      // Adapt the values tag to Helm standards:
      String adaptedString = Serialization.yamlMapper().writeValueAsString(resource)
          .replaceAll(Pattern.quote("\"" + VALUES_START_TAG), VALUES_START_TAG)
          .replaceAll(Pattern.quote(VALUES_END_TAG + "\""), VALUES_END_TAG);

      writeFile(adaptedString, targetFile);
    }

    return Collections.emptyMap();
  }

  private List<Map<Object, Object>> replaceValuesInYamls(Collection<File> generatedFiles,
      List<ConfigReference> valuesReferences,
      Map<String, Object> prodValues,
      Map<String, Map<String, Object>> valuesByProfile) throws IOException {

    List<Map<Object, Object>> allResources = new LinkedList<>();
    for (File generatedFile : generatedFiles) {
      if (!generatedFile.getName().toLowerCase().matches(YAML_REG_EXP)) {
        continue;
      }

      // Yaml as map of resources
      List<Map<Object, Object>> resources = Serialization.unmarshalAsListOfMaps(generatedFile.toPath());

      // Read helm expression parsers
      HelmExpressionParser parser = new HelmExpressionParser(resources);

      for (ConfigReference valueReference : valuesReferences) {
        String valueReferenceProperty = Strings.kebabToCamelCase(valueReference.getProperty());

        // Check whether path exists
        for (String path : valueReference.getPaths()) {
          Object found = parser.readAndSet(path, VALUES_START_TAG + valueReferenceProperty + VALUES_END_TAG);

          Object value = Optional.ofNullable(valueReference.getValue()).orElse(found);
          if (value != null) {
            String valueProfile = valueReference.getProfile();
            Map<String, Object> values = prodValues;
            if (Strings.isNotNullOrEmpty(valueProfile)) {
              values = valuesByProfile.get(valueProfile);
              if (values == null) {
                values = new HashMap<>();
                valuesByProfile.putIfAbsent(valueProfile, values);
              }
            }

            values.putIfAbsent(valueReferenceProperty, value);
          }
        }
      }

      allResources.addAll(resources);
    }

    return allResources;
  }

  private Map<String, String> createChartYaml(HelmChartConfig helmConfig, Project project, Path outputDir) throws IOException {
    final Chart chart = new Chart();
    chart.setApiVersion(CHART_API_VERSION);
    chart.setName(helmConfig.getName());
    chart.setVersion(getVersion(helmConfig, project));
    chart.setDescription(helmConfig.getDescription());
    chart.setHome(helmConfig.getHome());
    chart.setSources(Arrays.asList(helmConfig.getSources()));
    chart.setMaintainers(Arrays.stream(helmConfig.getMaintainers())
        .map(m -> new Maintainer(m.getName(), m.getEmail(), m.getUrl()))
        .collect(Collectors.toList()));
    chart.setIcon(helmConfig.getIcon());
    chart.setKeywords(Arrays.asList(helmConfig.getKeywords()));
    chart.setDependencies(Arrays.stream(helmConfig.getDependencies())
        .map(d -> new HelmDependency(d.getName(),
            Strings.defaultIfEmpty(d.getAlias(), d.getName()),
            d.getVersion(),
            d.getRepository()))
        .collect(Collectors.toList()));

    Path yml = getChartOutputDir(helmConfig, outputDir).resolve(CHART_FILENAME).normalize();
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

  private Path getChartOutputDir(HelmChartConfig helmConfig, Path outputDir) {
    return outputDir.resolve(HELM).resolve(helmConfig.getName());
  }

  private static List<File> listYamls(Path directory) {
    return Stream.of(Optional.ofNullable(directory.toFile().listFiles()).orElse(new File[0]))
        .filter(File::isFile)
        .filter(f -> f.getName().toLowerCase().matches(YAML_REG_EXP))
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
