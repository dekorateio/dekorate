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

import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRunSpecFluent;

public class AddServiceAccountToTaskDecorator extends NamedResourceDecorator<PipelineRunSpecFluent> {

  private final String task;
  private final String serviceAccount;

  public AddServiceAccountToTaskDecorator(String task, String serviceAccount) {
    this(ANY, task, serviceAccount);
  }

  public AddServiceAccountToTaskDecorator(String name, String task, String serviceAccount) {
    super(name);
    this.task = task;
    this.serviceAccount = serviceAccount;
  }

  @Override
  public void andThenVisit(PipelineRunSpecFluent item, ObjectMeta resourceMeta) {
    item.withServiceAccountName(serviceAccount);
  }
}
