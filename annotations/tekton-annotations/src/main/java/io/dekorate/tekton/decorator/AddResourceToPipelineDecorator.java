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

public class AddResourceToPipelineDecorator extends NamedPipelineDecorator {

  private final String type;
  private final String name;
  private final Boolean optional;

  public AddResourceToPipelineDecorator(String pipelineName, String type, String name, Boolean optional) {
    super(pipelineName);
    this.type = type;
    this.name = name;
    this.optional = optional;
  }

  @Override
  public void andThenVisit(PipelineSpecFluent<?> pipelineSpec) {
    pipelineSpec.removeMatchingFromResources(r -> name.equals(r.getName()));

    pipelineSpec.addNewResource()
      .withType(type)
      .withName(name)
      .withOptional(optional)
      .endResource();
  }
}
