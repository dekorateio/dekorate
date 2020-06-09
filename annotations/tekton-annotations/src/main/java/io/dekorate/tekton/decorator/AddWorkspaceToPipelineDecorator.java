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

import io.fabric8.tekton.pipeline.v1beta1.PipelineSpecFluent;

public class AddWorkspaceToPipelineDecorator extends NamedPipelineDecorator {

  private final String workspace;
  private final String description;

public AddWorkspaceToPipelineDecorator(String pipelineName, String workspace, String description) {
  super(pipelineName);
  this.workspace = workspace;
  this.description = description;
}

  @Override
  public void andThenVisit(PipelineSpecFluent<?> pipelineSpec) {
    pipelineSpec.removeMatchingFromWorkspaces(w -> workspace.equals(w.getName()));

    pipelineSpec.addNewWorkspace()
      .withName(workspace)
      .withDescription(description)
      .endWorkspace();
  }
}
