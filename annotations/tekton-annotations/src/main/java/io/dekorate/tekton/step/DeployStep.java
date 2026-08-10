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

package io.dekorate.tekton.step;

import java.nio.file.Path;

import io.dekorate.project.Project;

public class DeployStep implements Step {

  public static final String PATH_TO_YML_PARAM_NAME = "pathToYml";
  public static final String PATH_TO_YML_PARAM_DESCRIPTION = "Path to yml";
  private static final String PATH_TO_YML_PARAM_DEFAULT = "target/classes/META-INF/dekorate/kubernetes.yml";

  public static final String getYamlPath(Project project) {
    Path root = project != null && project.getScmInfo() != null ? project.getScmInfo().getRoot() : null;
    Path module = project != null ? project.getRoot() : null;

    if (root != null && module != null) {
      return module.toAbsolutePath().resolve(PATH_TO_YML_PARAM_DEFAULT).toAbsolutePath().toString()
          .substring(root.toAbsolutePath().toString().length() + 1);
    } else {
      return PATH_TO_YML_PARAM_DEFAULT;
    }
  }

}
