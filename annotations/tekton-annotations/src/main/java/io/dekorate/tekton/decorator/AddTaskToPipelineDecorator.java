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

import java.util.Map;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.tekton.pipeline.v1beta1.Param;
import io.fabric8.tekton.pipeline.v1beta1.ParamBuilder;
import io.fabric8.tekton.pipeline.v1beta1.PipelineSpecFluent;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTask;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTaskBuilder;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTaskFluent;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTaskInputResource;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTaskInputResourceBuilder;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTaskOutputResource;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTaskOutputResourceBuilder;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTaskResourcesBuilder;
import io.fabric8.tekton.pipeline.v1beta1.WorkspacePipelineTaskBinding;
import io.fabric8.tekton.pipeline.v1beta1.WorkspacePipelineTaskBindingBuilder;

public class AddTaskToPipelineDecorator extends NamedPipelineDecorator {

  private PipelineTask pipelineTask;

  public AddTaskToPipelineDecorator(String pipelineName, PipelineTask pipelineTask) {
    super(pipelineName);
    this.pipelineTask = pipelineTask;
  }

  public AddTaskToPipelineDecorator(String pipelineName, String name, String ref, Map<String, String> workspaces, Map<String, String> params, Map<String, String> inputs, Map<String, String> outputs) {
    this(pipelineName,
         new PipelineTaskBuilder()
         .withName(name)
         .withNewTaskRef().withName(ref).endTaskRef()
         .withWorkspaces(workspaces.entrySet().stream().map(e -> workspace(e.getKey(), e.getValue())).collect(Collectors.toList()))
         .withParams(params.entrySet().stream().map(e -> param(e.getKey(), e.getValue())).collect(Collectors.toList()))
         .accept(new TypedVisitor<PipelineTaskFluent<?>>() {
             @Override
             public void visit(PipelineTaskFluent<?> t) {
               if (!inputs.isEmpty() || !outputs.isEmpty()) {
                 PipelineTaskResourcesBuilder b = new PipelineTaskResourcesBuilder();
                 if (!inputs.isEmpty()) {
                   b.withInputs(inputs.entrySet().stream().map(e -> input(e.getKey(), e.getValue())).collect(Collectors.toList()));
                 }
                 if (!outputs.isEmpty()) {
                   b.withOutputs(outputs.entrySet().stream().map(e -> output(e.getKey(), e.getValue())).collect(Collectors.toList()));
                 }
               }
             }
         }).build());
  }

  @Override
  public void andThenVisit(PipelineSpecFluent<?> pipelineSpec) {
    pipelineSpec.addToTasks(pipelineTask);
  }

  private static WorkspacePipelineTaskBinding workspace(String name, String workspace) {
    return new WorkspacePipelineTaskBindingBuilder().withName(name).withWorkspace(workspace).build();
  }

  private static Param param(String name, String value) {
    return new ParamBuilder().withName(name).withNewValue(value).build();
  }

  private static PipelineTaskInputResource input(String name, String value) {
    return new PipelineTaskInputResourceBuilder().withName(name).withResource(value).build();
  }

  private static PipelineTaskOutputResource output(String name, String value) {
    return new PipelineTaskOutputResourceBuilder().withName(name).withResource(value).build();
  }
}
