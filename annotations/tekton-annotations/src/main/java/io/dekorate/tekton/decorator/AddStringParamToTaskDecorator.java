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

import io.dekorate.deps.tekton.pipeline.v1beta1.TaskSpecFluent;

public class AddStringParamToTaskDecorator extends NamedTaskDecorator {

  private final String name;
  private final String description;
  private final String defaultValue;

  private static final String TYPE = "string";

  public AddStringParamToTaskDecorator(String taskName, String name, String description, String defaultValue) {
    super(taskName);
    this.name = name;
    this.description = description;
    this.defaultValue = defaultValue;
  }

  @Override
  public void andThenVisit(TaskSpecFluent<?> taskSpec) {
    taskSpec.addNewParam()
        .withName(name)
        .withDescription(description)
        .withType(TYPE)
        .withNewDefault().withStringVal(defaultValue).endDefault()
        .endParam();
  }
}
