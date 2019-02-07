package io.ap4k;

import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.project.Project;

import java.util.Map;
import java.util.Set;

public interface SessionWriter extends WithProject {

  String PACKAGE = "";
  String FILENAME = "%s.%s";
  String CONFIG = ".config/%s.%s";
  String PROJECT = "META-INF/ap4k/.project.%s";
  String[] STRIP = {"^Editable", "Config$"};
  String JSON = "json";
  String YML = "yml";
  String TMP = "tmp";
  String DOT = ".";

  /**
   * Writes all {@link Session} resources.
   * @param session The target session.
   */
  default void write(Session session) {
    session.close();
    Map<String, KubernetesList> resources = session.getGeneratedResources();
    Set<? extends Configuration> configurations = session.configurators().toSet();
    resources.forEach((g, l) -> write(g, l));
    configurations.forEach(c -> write(c));
    write(getProject());
  }

  /**
   * Writes a {@link Configuration}.
   * @param config  The target session configurations.
   */
   void write(Configuration config);

  /**
   * Writes a {@link Project}.
   * @param project  The target session configurations.
   */
   void write(Project project);

  /**
   * Writes the specified resource list under the specified group file.
   * @param group   The group name.
   * @param list    The resource list.
   */
   void write(String group, KubernetesList list);
}
