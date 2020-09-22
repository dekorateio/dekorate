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
package io.dekorate.knative.decorator;

import io.dekorate.knative.config.AutoScalerClass;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.fabric8.kubernetes.api.model.ConfigMapFluent;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class ApplyGlobalAutoscalingClassDecorator extends NamedResourceDecorator<ConfigMapFluent<?>> {

  private static final String POD_AUTOSCALER_CLASS = "pod-autoscaler-class";

  private static final String AUTOSCALING_CLASS_SUFFIX = ".autoscaling.knative.dev";

  private final AutoScalerClass clazz;

  public ApplyGlobalAutoscalingClassDecorator(AutoScalerClass clazz) {
    super("config-autoscaler");
    this.clazz = clazz;
  }

  @Override
  public void andThenVisit(ConfigMapFluent<?> config, ObjectMeta resourceMeta) {
    config.addToData(POD_AUTOSCALER_CLASS, clazz.name().toLowerCase() + AUTOSCALING_CLASS_SUFFIX);
  }
}
