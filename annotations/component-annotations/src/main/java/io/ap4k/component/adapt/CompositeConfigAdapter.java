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
package io.ap4k.component.adapt;

import io.ap4k.component.annotation.CompositeApplication;
import io.ap4k.component.config.CompositeConfigBuilder;
import io.ap4k.component.config.Link;
import io.ap4k.kubernetes.config.Env;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CompositeConfigAdapter {

  public static CompositeConfigBuilder newBuilder(CompositeApplication instance) {
    return new CompositeConfigBuilder()
      .withName(instance.name())
      .withDeploymentType(instance.deploymentType())
      .withExposeService(instance.exposeService())
      .addAllToEnvVars(Arrays.asList(instance.envVars()).stream().map(i ->new Env(i.name(), i.value(), i.secret(), i.configmap(), i.field())).collect(Collectors.toList()))
      .addAllToLinks(Arrays.asList(instance.links()).stream().map(i ->new Link(i.name(), i.targetcomponentname(), i.kind(), i.ref(), Arrays.asList(i.envVars()).stream().map(e ->new Env(e.name(), e.value(), e.secret(), e.configmap(), e.field())).collect(Collectors.toList()).toArray(new Env[i.envVars().length]))).collect(Collectors.toList()));
  }
}
