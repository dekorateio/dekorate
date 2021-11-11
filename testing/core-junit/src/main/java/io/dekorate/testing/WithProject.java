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
package io.dekorate.testing;

import static io.dekorate.testing.Testing.DEKORATE_STORE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.dekorate.DekorateException;
import io.dekorate.project.FileProjectFactory;
import io.dekorate.project.Project;
import io.dekorate.utils.Serialization;

public interface WithProject {

  String PROJECTS = "PROJECTS";
  String PROJECT_YML = ".project.yml";

  String[] getAdditionalModules(ExtensionContext context);

  default List<Project> getProjects(ExtensionContext context) {
    Object projectsInStore = context.getStore(DEKORATE_STORE).get(PROJECTS);
    if (projectsInStore != null && projectsInStore instanceof List) {
      return (List<Project>) projectsInStore;
    }

    try {
      List<String> projectsLocations = new ArrayList<>();
      // Current project
      projectsLocations.add(".");
      // Additional modules
      projectsLocations.addAll(Arrays.asList(getAdditionalModules(context)));

      List<Project> projects = new ArrayList<>();

      for (String projectLocation : projectsLocations) {
        // Get project to get the build info
        Project projectInfo = new FileProjectFactory().create(new File(projectLocation));
        // Class output dir might be either `target/classes` if Maven or `build/classes` for gradle,
        // but the .project yaml files are located at `target/.dekorate/` or `build/.dekorate/`, so we need the parent of the
        // class output dir:
        Path outputPath = Paths.get(projectLocation).resolve(projectInfo.getBuildInfo().getClassOutputDir().getParent());
        Files.walk(outputPath)
            .filter(path -> path.getFileName().toString().endsWith(PROJECT_YML))
            .map(path -> {
              try (InputStream is = new FileInputStream(path.toFile())) {
                return Serialization.unmarshal(is, Project.class);
              } catch (IOException e) {
                throw DekorateException.launderThrowable(e);
              }
            })
            .forEach(projects::add);
      }

      context.getStore(DEKORATE_STORE).put(PROJECTS, projects);

      return projects;
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
  }
}
