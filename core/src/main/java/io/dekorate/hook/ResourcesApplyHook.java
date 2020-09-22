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

package io.dekorate.hook;

import io.dekorate.project.Project;
import io.dekorate.utils.Exec;

public class ResourcesApplyHook extends ProjectHook {

  private final Exec.ProjectExec exec;
  private final String group;
  private final String command;
  private final String path;

  public ResourcesApplyHook(Project project, String group, String command) {
    super(project);
    this.exec = Exec.inProject(project);
    this.group = group;
    this.command = command;
    this.path = project.getBuildInfo().getClassOutputDir().resolve(project.getDekorateOutputDir()).resolve(group + ".yml")
        .toAbsolutePath().toString();
  }

  @Override
  public void run() {
    exec.commands(command, "apply", "--force", "-f", path);
  }

  @Override
  public void init() {

  }

  @Override
  public void warmup() {

  }

}
