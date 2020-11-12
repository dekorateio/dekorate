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

import io.dekorate.deps.tekton.pipeline.v1beta1.PipelineSpecFluent;
import io.dekorate.utils.Strings;
import io.dekorate.deps.kubernetes.api.builder.Predicate;
import io.dekorate.deps.tekton.pipeline.v1beta1.PipelineSpecFluent;
import io.dekorate.deps.tekton.pipeline.v1beta1.PipelineTaskBuilder;

public class AddWorkspaceToPipelineTaskDecorator extends NamedPipelineDecorator {

  private final String taskName;
  private final String id;
  private final String workspace;

  public AddWorkspaceToPipelineTaskDecorator(String pipelineName, String taskName, String id, String workspace) {
    super(pipelineName);
    this.taskName = taskName;
    this.id = id;
    this.workspace = workspace;
  }

  @Override
  public void andThenVisit(PipelineSpecFluent<?> spec) {
    Predicate<PipelineTaskBuilder> predicate = new Predicate<PipelineTaskBuilder>() {
        @Override
        public Boolean apply(PipelineTaskBuilder task) {
          return Strings.isNullOrEmpty(taskName) || taskName.equals(task.getName());
        }
      };

    //If no task name is specified we need to add the workspace to all pipeline tasks.
    if (spec.hasMatchingTask(predicate)) {
      spec.editMatchingTask(predicate).addNewWorkspace().withName(id).withWorkspace(workspace).endWorkspace().endTask();
    }
  }
}
