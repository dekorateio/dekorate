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

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Session;
import io.dekorate.helm.config.HelmBuildConfig;
import io.dekorate.helm.model.Chart;
import io.dekorate.helm.model.HelmDependency;
import io.dekorate.helm.model.Maintainer;
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
    Map<String, Object> values = session.getResourceRegistry().getConfigReferences();
    session.getConfigurationRegistry().get(HelmBuildConfig.class).ifPresent(helmConfig -> {
      if (helmConfig.isEnabled()) {
        try {
          LOGGER.info(String.format("Creating Helm Chart \"%s\"", helmConfig.getChart()));
          artifacts.putAll(processSourceFiles(values));
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

  private Map<String, String> createValuesYaml(Map<String, Object> values) throws IOException {
    Path valuesFile = getOutputDir().resolve(VALUES_FILENAME);
    return writeFileAsYaml(values, valuesFile);
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

  private Map<String, String> processSourceFiles(Map<String, Object> values) throws IOException {
    Map<String, String> sourceFiles = new HashMap<>();

    StringBuilder sb = new StringBuilder();
    Path templatesDir = getOutputDir().resolve(TEMPLATES);
    Files.createDirectory(templatesDir);
    for (File file : listYamls(getOutputDir())) {
      List<Map<Object, Object>> resourceList = Serialization.unmarshalAsListOfMaps(file.toPath());

      for (String configReference : values.keySet()) {
        String[] configReferencePath = configReference.split(Pattern.quote("."));
        if (configReferencePath.length > 1) {
          String kind = configReferencePath[0];
          for (Map<Object, Object> resource : resourceList) {
            if (kind.equalsIgnoreCase((String) resource.get("kind"))) {
              Map<Object, Object> pointer = resource;
              for (int index = 1; index < configReferencePath.length - 1; index++) {
                String field = configReferencePath[index];
                Object value = resource.get(field);
                if (value instanceof Map) {
                  pointer = (Map) value;
                }
              }

              pointer.put(configReferencePath[configReferencePath.length - 1], "{{ .Values." + configReference + " }}");
            }

            sb.append(Serialization.asYaml(resource));
          }

        }
      }

      Path targetFile = templatesDir.resolve(file.getName());
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
