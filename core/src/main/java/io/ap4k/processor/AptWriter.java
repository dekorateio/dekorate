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

import io.ap4k.Session;
import io.ap4k.SessionWriter;
import io.ap4k.WithProject;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.project.Project;
import io.ap4k.utils.Serialization;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AptWriter implements SessionWriter, WithProject {

  protected static final String PACKAGE = "";
  protected static final String FILENAME = "%s.%s";
  protected static final String CONFIG = ".config/%s.%s";
  protected static final String PROJECT = "META-INF/ap4k/.project.%s";
  protected static final String[] STRIP = {"^Editable", "Config$"};
  protected static final String JSON = "json";
  protected static final String YML = "yml";
  protected static final String TMP = "tmp";
  protected static final String DOT = ".";

  private final ProcessingEnvironment processingEnv;

  public AptWriter(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  /**
   * Writes all {@link Session} resources.
   * @param session The target session.
   * @return Map containing the file system paths of the output files as keys and their actual content as the values
   */
  public Map<String, String> write(Session session) {
    session.close();
    Map<String, KubernetesList> resources = session.getGeneratedResources();
    Set<? extends Configuration> configurations = session.configurators().toSet();
    resources.forEach((g, l) -> write(g, l));
    configurations.forEach(c -> write(c));
    write(getProject());
    return null;
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
      FileObject yml = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, PACKAGE, getProject().getAp4kOutputDir() + "/" + String.format(CONFIG, name, YML));
      try (Writer writer = yml.openWriter()) {
        final String value = Serialization.asYaml(config);
        writer.write(value);
        return new AbstractMap.SimpleEntry<>(yml.toString(), value);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing resources", e);
    }
  }

  /**
   * Writes a {@link Project}.
   * @param project  The project.
   * @return Map Entry containing the file system path of the written project and the actual content as the value
   */
  public Map.Entry<String, String> write(Project project) {
    try {
      FileObject yml = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, PACKAGE, String.format(PROJECT, YML));
      try (Writer writer = yml.openWriter()) {
        final String value = Serialization.asYaml(project);
        writer.write(value);
        return new AbstractMap.SimpleEntry<>(yml.toString(), value);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing resources", e);
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
      final Map<String, String> result = new HashMap<>();
      FileObject json = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, PACKAGE, getProject().getAp4kOutputDir() + "/" + String.format(FILENAME, group, JSON));
      try (Writer writer = json.openWriter()) {
        final String jsonValue = Serialization.asJson(list);
        writer.write(jsonValue);
        result.put(json.getName(), jsonValue);
      }
      FileObject yml = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, PACKAGE, getProject().getAp4kOutputDir() + "/" + String.format(FILENAME, group, YML));
      try (Writer writer = yml.openWriter()) {
        final String yamlValue = Serialization.asYaml(list);
        writer.write(yamlValue);
        result.put(yml.getName(), yamlValue);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing resources", e);
    }
    return null;
  }
}
