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

package io.dekorate.tekton.decorator;

import java.nio.file.Path;

import io.dekorate.project.Project;
import io.dekorate.utils.Strings;

public class AddContextParamToTaskDecorator extends AddParamToTaskDecorator {

  private static final String PATH_TO_CONTEXT_PARAM_NAME = "pathToContext";
  private static final String PATH_TO_CONTEXT_DESCRIPTION = "Path to context. Usually refers to module directory";

  public AddContextParamToTaskDecorator(String taskName, Project project) {
    super(taskName, PATH_TO_CONTEXT_PARAM_NAME, PATH_TO_CONTEXT_DESCRIPTION, getContextPath(project));
  }

  public static final String getContextPath(Project project) {
    Path root = project != null && project.getScmInfo() != null ? project.getScmInfo().getRoot() : null;
    Path module = project != null ? project.getRoot() : null;

    String result = "";
    if (root != null && module != null) {
      result = module.toAbsolutePath().toString().substring(root.toAbsolutePath().toString().length());
    }
    if (Strings.isNullOrEmpty(result)) {
      result = "./";
    }
    return result;
  }

}
