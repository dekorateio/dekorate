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

public class SimpleFileWriter implements SessionWriter, WithProject {

  private final Path outputdir;

  public SimpleFileWriter(Path outputdir) {
    this.outputdir = outputdir;
  }

  /**
   * Writes a {@link Configuration}.
   * @param config  The target session configurations.
   */
  public void write(Configuration config) {
    try {
      String name = config.getClass().getSimpleName();
      for (String s : STRIP) {
        name = name.replaceAll(s, "");
      }
      name = name.toLowerCase();
      Path yml = outputdir.resolve(String.format(CONFIG, name, YML));
      yml.toFile().getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(yml.toFile())) {
        writer.write(Serialization.asYaml(config));
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing resources");
    }
  }

  /**
   * Writes a {@link Project}.
   * @param project  The project.
   */
  public void write(Project project) {
    try {
      Path yml = outputdir.resolve(String.format(PROJECT, YML));
      yml.toFile().getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(yml.toFile())) {
        writer.write(Serialization.asYaml(project));
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing resources");
    }
  }

  /**
   * Write the resources contained in the {@link KubernetesList} in a directory named after the specififed group.
   * @param group The group.
   * @param list  The resource list.
   */
  public void write(String group, KubernetesList list) {
    try {
      Path json = outputdir.resolve(String.format(FILENAME, group, JSON));
      try (FileWriter writer = new FileWriter(json.toFile())) {
        writer.write(Serialization.asJson(list));
      }
      Path yml = outputdir.resolve(String.format(FILENAME, group, JSON));
      yml.toFile().getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(yml.toFile())) {
        writer.write(Serialization.asYaml(list));
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing resources");
    }
  }
}
