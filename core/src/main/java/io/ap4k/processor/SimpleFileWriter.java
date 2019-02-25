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
package io.ap4k.processor;

import io.ap4k.SessionWriter;
import io.ap4k.WithProject;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.project.Project;
import io.ap4k.utils.Serialization;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class SimpleFileWriter implements SessionWriter, WithProject {

  private final Path outputdir;
  private final boolean doWrite;

  public SimpleFileWriter(Path outputdir) {
    this(outputdir, true);
  }

  public SimpleFileWriter(Path outputdir, boolean doWrite) {
    this.outputdir = outputdir;
    this.doWrite = doWrite;
  }

  /**
   * Writes a {@link Configuration}.
   * @param config  The target session configurations.
   * @return Map Entry containing the file system path of the written configuration and the actual content as the value
   */
  public Map.Entry<String, String> write(Configuration config) {
    try {
      String name = config.getClass().getSimpleName();
      for (String s : STRIP) {
        name = name.replaceAll(s, "");
      }
      name = name.toLowerCase();
      final Path yml = outputdir.resolve(String.format(CONFIG, name, YML));
      final String value = Serialization.asYaml(config);
      if (doWrite) {
        yml.toFile().getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(yml.toFile())) {
          writer.write(value);
          return new AbstractMap.SimpleEntry<>(yml.toString(), value);
        }
      } else {
        return new AbstractMap.SimpleEntry<>(yml.toString(), value);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing resources");
    }
  }

  /**
   * Writes a {@link Project}.
   * @param project  The project.
   * @return Map Entry containing the file system path of the written project and the actual content as the value
   */
  public Map.Entry<String, String> write(Project project) {
    try {
      final Path yml = outputdir.resolve(String.format(PROJECT_ONLY, YML));
      final String value = Serialization.asYaml(project);
      if (doWrite) {
        yml.toFile().getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(yml.toFile())) {
          writer.write(value);
          return new AbstractMap.SimpleEntry<>(yml.toString(), value);
        }
      } else {
        return new AbstractMap.SimpleEntry<>(yml.toString(), value);
      }

    } catch (IOException e) {
      throw new RuntimeException("Error writing resources");
    }
  }

  /**
   * Write the resources contained in the {@link KubernetesList} in a directory named after the specififed group.
   * @param group The group.
   * @param list  The resource list.
   * @return Map containing the file system paths of the output files as keys and their actual content as the values
   */
  public Map<String, String> write(String group, KubernetesList list) {
    try {
      //write json representation
      final Map<String, String> result = new HashMap<>();
      final Path json = outputdir.resolve(String.format(FILENAME, group, JSON));
      final String jsonValue = Serialization.asJson(list);
      if (doWrite) {
        json.toFile().getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(json.toFile())) {
          writer.write(jsonValue);
          result.put(json.toString(), jsonValue);
        }
      } else {
        result.put(json.toString(), jsonValue);
      }

      //write yml representation
      final Path yml = outputdir.resolve(String.format(FILENAME, group, YML));
      final String yamlValue = Serialization.asYaml(list);
      if (doWrite) {
        yml.toFile().getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(yml.toFile())) {

          writer.write(yamlValue);
          result.put(yml.toString(), yamlValue);
        }
      } else {
        result.put(yml.toString(), yamlValue);
      }

      return result;
    } catch (IOException e) {
      throw new RuntimeException("Error writing resources");
    }
  }
}
