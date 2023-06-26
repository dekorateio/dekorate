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

import io.dekorate.utils.Strings;

public interface StepDecorator {

  char SLASH = '/';
  String SOURCE = "source";
  String WORKSPACE_PATH = "$(workspaces.%s.path)";

  default String sourcePath(String... path) {
    return workspacePath(SOURCE, path);
  }

  default String workspacePath(String workspace, String... path) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format(WORKSPACE_PATH, workspace));
    if (path != null && path.length > 0) {
      sb.append(SLASH);
      sb.append(Strings.join(path, SLASH));
    }
    return sb.toString();
  }
}
