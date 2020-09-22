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
package io.dekorate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.project.Project;
import io.fabric8.kubernetes.api.model.KubernetesList;

public interface SessionWriter extends WithProject {

  String PACKAGE = "";
  String FILENAME = "%s.%s";
  String CONFIG = "config/%s.%s";
  String PROJECT_ONLY = ".project.%s";
  String PROJECT = "META-INF/dekorate/" + PROJECT_ONLY;
  String[] STRIP = { "^Editable", "BuildConfig$", "Config$" };
  String JSON = "json";
  String YML = "yml";
  String TMP = "tmp";
  String DOT = ".";

  default Set<String> getWhitelistedGroups() {
    return Collections.EMPTY_SET;
  }

  /**
   * Writes all {@link Session} resources.
   * 
   * @param session The target session.
   * @return Map containing the file system paths of the output files as keys and their actual content as the values
   */
  default Map<String, String> write(Session session) {
    final Map<String, String> result = new HashMap<>();
    session.close();
    Map<String, KubernetesList> resources = session.getGeneratedResources();
    Set<? extends Configuration> configurations = session.configurators().toSet();
    Set<String> whitelist = getWhitelistedGroups();
    resources.entrySet().stream().filter(e -> whitelist.isEmpty() || whitelist.contains(e.getKey())).forEach(e -> {
      result.putAll(write(e.getKey(), e.getValue()));
    });
    configurations.forEach(c -> {
      final Map.Entry<String, String> entry = write(c);
      result.put(entry.getKey(), entry.getValue());
    });
    final Map.Entry<String, String> projectEntry = write(getProject());
    result.put(projectEntry.getKey(), projectEntry.getValue());
    return result;
  }

  /**
   * Writes a {@link Configuration}.
   * 
   * @param config The target session configurations.
   * @return Map Entry containing the file system path of the written configuration and the actual content as the value
   */
  Map.Entry<String, String> write(Configuration config);

  /**
   * Writes a {@link Project}.
   * 
   * @param project The target session configurations.
   * @return Map Entry containing the file system path of the written project and the actual content as the value
   */
  Map.Entry<String, String> write(Project project);

  /**
   * Writes the specified resource list under the specified group file.
   * 
   * @param group The group name.
   * @param list The resource list.
   * @return Map containing the file system paths of the output files as keys and their actual content as the values
   */
  Map<String, String> write(String group, KubernetesList list);
}
