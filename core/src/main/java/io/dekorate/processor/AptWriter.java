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

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Session;
import io.dekorate.SessionWriter;
import io.dekorate.WithProject;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.project.Project;
import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.KubernetesList;

public class AptWriter extends SimpleFileWriter implements SessionWriter, WithProject {

  protected static final String PACKAGE = "";
  protected static final String FILENAME = "%s.%s";
  protected static final String CONFIG = ".config/%s.%s";
  protected static final String PROJECT = "META-INF/dekorate/.project.%s";
  protected static final String[] STRIP = { "^Editable", "BuildConfig$", "Config$" };
  protected static final String JSON = "json";
  protected static final String YML = "yml";
  protected static final String TMP = "tmp";
  protected static final String DOT = ".";

  private final ProcessingEnvironment processingEnv;
  private final Logger LOGGER = LoggerFactory.getLogger();

  public AptWriter(Project project, ProcessingEnvironment processingEnv) {
    super(project);
    this.processingEnv = processingEnv;
  }

  /**
   * Writes all {@link Session} resources.
   * 
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
   * Write the resources contained in the {@link KubernetesList} in a directory named after the specififed group.
   * 
   * @param group The group.
   * @param list The resource list.
   * @return Map containing the file system paths of the output files as keys and their actual content as the values
   */
  public Map<String, String> write(String group, KubernetesList list) {

    try {
      final Map<String, String> result = new HashMap<>();
      FileObject json = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, PACKAGE,
          getProject().getDekorateOutputDir() + "/" + String.format(FILENAME, group, JSON));
      try (Writer writer = json.openWriter()) {
        final String jsonValue = Serialization.asJson(list);
        LOGGER.info("Writing: " + json.toUri());
        writer.write(jsonValue);
        result.put(json.getName(), jsonValue);
      }
      FileObject yml = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, PACKAGE,
          getProject().getDekorateOutputDir() + "/" + String.format(FILENAME, group, YML));
      try (Writer writer = yml.openWriter()) {
        final String yamlValue = Serialization.asYaml(list);
        LOGGER.info("Writing: " + yml.toUri());
        writer.write(yamlValue);
        result.put(yml.getName(), yamlValue);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing resources", e);
    }
    return null;
  }
}
