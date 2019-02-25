package io.ap4k;

import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.project.Project;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface SessionWriter extends WithProject {

  String PACKAGE = "";
  String FILENAME = "%s.%s";
  String CONFIG = ".config/%s.%s";
  String PROJECT_ONLY = ".project.%s";
  String PROJECT = "META-INF/ap4k/" + PROJECT_ONLY;
  String[] STRIP = {"^Editable", "Config$"};
  String JSON = "json";
  String YML = "yml";
  String TMP = "tmp";
  String DOT = ".";

  /**
   * Writes all {@link Session} resources.
   * @param session The target session.
   * @return Map containing the file system paths of the output files as keys and their actual content as the values
   */
  default Map<String, String> write(Session session) {
    final Map<String, String> result = new HashMap<>();
    session.close();
    Map<String, KubernetesList> resources = session.getGeneratedResources();
    Set<? extends Configuration> configurations = session.configurators().toSet();
    resources.forEach((g, l) -> {
      result.putAll(write(g, l));
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
   * @param config  The target session configurations.
   * @return Map Entry containing the file system path of the written configuration and the actual content as the value
   */
  Map.Entry<String, String> write(Configuration config);

  /**
   * Writes a {@link Project}.
   * @param project  The target session configurations.
   * @return Map Entry containing the file system path of the written project and the actual content as the value
   */
   Map.Entry<String, String> write(Project project);

  /**
   * Writes the specified resource list under the specified group file.
   * @param group   The group name.
   * @param list    The resource list.
   * @return Map containing the file system paths of the output files as keys and their actual content as the values
   */
  Map<String, String> write(String group, KubernetesList list);
}
