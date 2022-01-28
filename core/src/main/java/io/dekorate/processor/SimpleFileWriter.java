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
package io.dekorate.processor;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.dekorate.SessionWriter;
import io.dekorate.WithProject;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.project.Project;
import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.KubernetesList;

public class SimpleFileWriter implements SessionWriter, WithProject {

  private final Path metaDir;
  private final Path outputDir;
  private final boolean doWrite;
  private final Set<String> whitelistedGroups;

  public SimpleFileWriter(Project project) {
    this(project, true);
  }

  public SimpleFileWriter(Project project, boolean doWrite) {
    this(project.getBuildInfo().getClassOutputDir().resolve(project.getDekorateMetaDir()),
        project.getBuildInfo().getClassOutputDir().resolve(project.getDekorateOutputDir()), doWrite);
  }

  public SimpleFileWriter(Path metaDir, Path outputDir) {
    this(metaDir, outputDir, true);
  }

  public SimpleFileWriter(Path metaDir, Path outputDir, boolean doWrite, String... whitelistedGroups) {
    this(metaDir, outputDir, doWrite, new HashSet(Arrays.asList(whitelistedGroups)));
  }

  public SimpleFileWriter(Path metaDir, Path outputDir, boolean doWrite, Set<String> whitelistedGroups) {
    this.metaDir = metaDir;
    this.outputDir = outputDir;
    this.doWrite = doWrite;
    this.whitelistedGroups = whitelistedGroups;
  }

  /**
   * Writes a {@link Configuration}.
   * 
   * @param config The target session configurations.
   * @return Map Entry containing the file system path of the written configuration and the actual content as the value
   */
  public Map.Entry<String, String> write(Configuration config) {
    try {
      String name = config.getClass().getSimpleName();
      for (String s : STRIP) {
        name = name.replaceAll(s, "");
      }
      name = name.toLowerCase();
      final Path yml = metaDir.resolve(String.format(CONFIG, name, YML)).normalize();
      final String value = Serialization.asYaml(config);
      if (doWrite) {
        yml.getParent().normalize().toFile().mkdirs();
        try (FileWriter writer = new FileWriter(yml.toFile())) {
          writer.write(value);
          return new AbstractMap.SimpleEntry<>(yml.toString(), value);
        }
      } else {
        return new AbstractMap.SimpleEntry<>(yml.toString(), value);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing resources", e);
    }
  }

  /**
   * Writes a {@link Project}.
   * 
   * @param project The project.
   * @return Map Entry containing the file system path of the written project and the actual content as the value
   */
  public Map.Entry<String, String> write(Project project) {
    try {
      final Path yml = metaDir.resolve(String.format(PROJECT_ONLY, YML)).normalize();
      final String value = Serialization.asYaml(project);
      if (doWrite) {
        yml.getParent().normalize().toFile().mkdirs();
        try (FileWriter writer = new FileWriter(yml.toFile())) {
          writer.write(value);
          return new AbstractMap.SimpleEntry<>(yml.toString(), value);
        }
      } else {
        return new AbstractMap.SimpleEntry<>(yml.toString(), value);
      }

    } catch (IOException e) {
      throw new RuntimeException("Error writing resources", e);
    }
  }

  /**
   * Write the resources contained in the {@link KubernetesList} in a directory named after the specififed group.
   * 
   * @param group The group.
   * @param list The resource list.
   * @return Map containing the file system paths of the output files as keys and their actual content as the values
   */
  public Map<String, String> write(String group, KubernetesList list) {

    try {
      //write json representation
      final Map<String, String> result = new HashMap<>();
      final Path json = outputDir.resolve(String.format(FILENAME, group, JSON)).normalize();
      final String jsonValue = Serialization.asJson(list);
      if (doWrite) {
        json.getParent().normalize().toFile().mkdirs();
        try (FileWriter writer = new FileWriter(json.toFile())) {
          writer.write(jsonValue);
          result.put(json.toString(), jsonValue);
        }
      } else {
        result.put(json.toString(), jsonValue);
      }

      //write yml representation
      final Path yml = outputDir.resolve(String.format(FILENAME, group, YML)).normalize();
      final String yamlValue = Serialization.asYaml(list);
      if (doWrite) {
        yml.getParent().normalize().toFile().mkdirs();
        try (FileWriter writer = new FileWriter(yml.toFile())) {

          writer.write(yamlValue);
          result.put(yml.toString(), yamlValue);
        }
      } else {
        result.put(yml.toString(), yamlValue);
      }

      return result;
    } catch (IOException e) {
      throw new RuntimeException("Error writing resources", e);
    }
  }

  public Set<String> getWhitelistedGroups() {
    return whitelistedGroups;
  }

  protected Path getOutputDir() {
    return outputDir;
  }

  protected Path getMetaDir() {
    return metaDir;
  }
}
