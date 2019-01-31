package io.ap4k;

import io.ap4k.project.Project;

import java.util.concurrent.atomic.AtomicReference;

public interface WithProject {

  AtomicReference<Project> project = new AtomicReference<>();

  default boolean projectExists() {
   return project.get() != null;
  }

  default Project getProject() {
    return WithProject.project.get();
  }

  default void setProject(Project project) {
    WithProject.project.set(project);
  }
}
