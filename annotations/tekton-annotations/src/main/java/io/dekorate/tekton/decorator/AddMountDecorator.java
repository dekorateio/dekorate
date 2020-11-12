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
 */
package io.dekorate.tekton.decorator;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.AddSidecarDecorator;
import io.dekorate.kubernetes.decorator.ApplyApplicationContainerDecorator;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.tekton.pipeline.v1beta1.StepFluent;

@Description("Add mount to the named step.")
public class AddMountDecorator extends NamedStepDecorator<StepFluent<?>> {

    private String name;
    private String path;
    private String subPath = "";
    private boolean readOnly = false;

  public AddMountDecorator(String name, String path, String subPath, boolean readOnly) {
    this(ANY, ANY, name, path, subPath, readOnly);
  }

  public AddMountDecorator(String taskName, String stepName, String name, String path, String subPath, boolean readOnly) {
    super(taskName, stepName);
    this.name = name;
    this.path = path;
    this.subPath = subPath;
    this.readOnly = readOnly;
  }

  @Override
  public void andThenVisit(StepFluent<?> step) {
    step.addToVolumeMounts(new VolumeMountBuilder()
        .withName(name)
        .withMountPath(path)
        .withSubPath(subPath)
        .withReadOnly(readOnly)
        .build());
  }

  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, ApplyApplicationContainerDecorator.class, AddSidecarDecorator.class };
  }


}
