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
package io.dekorate.kubernetes.adapter;

import io.dekorate.kubernetes.config.Env;
import io.dekorate.kubernetes.config.Mount;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.decorator.AddEnvVarDecorator;
import io.dekorate.kubernetes.decorator.AddLivenessProbeDecorator;
import io.dekorate.kubernetes.decorator.AddMountDecorator;
import io.dekorate.kubernetes.decorator.AddPortDecorator;
import io.dekorate.kubernetes.decorator.AddReadinessProbeDecorator;
import io.dekorate.kubernetes.decorator.AddStartupProbeDecorator;
import io.dekorate.kubernetes.decorator.ApplyImagePullPolicyDecorator;
import io.dekorate.kubernetes.decorator.ApplyLimitsCpuDecorator;
import io.dekorate.kubernetes.decorator.ApplyLimitsMemoryDecorator;
import io.dekorate.kubernetes.decorator.ApplyRequestsCpuDecorator;
import io.dekorate.kubernetes.decorator.ApplyRequestsMemoryDecorator;
import io.dekorate.utils.Probes;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;

public class ContainerAdapter {

  /**
   * Applies all container properties to the {@link ContainerBuilder}.
   * 
   * @param builder The container builder
   * @param container The container
   */
  public static void applyContainerToBuilder(ContainerBuilder builder, io.dekorate.kubernetes.config.Container container) {
    String name = container.getName();

    builder.withName(container.getName()).withImage(container.getImage()).withCommand(container.getCommand())
        .withArgs(container.getArguments());

    for (Env env : container.getEnvVars()) {
      builder.accept(new AddEnvVarDecorator(env));
    }
    for (Port port : container.getPorts()) {
      builder.accept(new AddPortDecorator(port));
    }
    for (Mount mount : container.getMounts()) {
      builder.accept(new AddMountDecorator(mount));
    }

    builder.accept(new ApplyImagePullPolicyDecorator(container.getImagePullPolicy()));

    //Probes
    if (Probes.isConfigured(container.getLivenessProbe())) {
      builder.accept(new AddLivenessProbeDecorator(name, container.getLivenessProbe()));
    }

    if (Probes.isConfigured(container.getReadinessProbe())) {
      builder.accept(new AddReadinessProbeDecorator(name, container.getReadinessProbe()));
    }

    if (Probes.isConfigured(container.getStartupProbe())) {
      builder.accept(new AddStartupProbeDecorator(name, container.getStartupProbe()));
    }

    // Container resources
    if (container.getLimitResources() != null && Strings.isNotNullOrEmpty(container.getLimitResources().getCpu())) {
      builder.accept(new ApplyLimitsCpuDecorator(name, container.getLimitResources().getCpu()));
    }

    if (container.getLimitResources() != null && Strings.isNotNullOrEmpty(container.getLimitResources().getMemory())) {
      builder.accept(new ApplyLimitsMemoryDecorator(name, container.getLimitResources().getMemory()));
    }

    if (container.getRequestResources() != null && Strings.isNotNullOrEmpty(container.getRequestResources().getCpu())) {
      builder.accept(new ApplyRequestsCpuDecorator(name, container.getRequestResources().getCpu()));
    }

    if (container.getRequestResources() != null && Strings.isNotNullOrEmpty(container.getRequestResources().getMemory())) {
      builder.accept(new ApplyRequestsMemoryDecorator(name, container.getRequestResources().getMemory()));
    }
  }

  /**
   * Adapt the dekorate {@link io.dekorate.kubernetes.config.Container} to a kubernetes model {@link Container}.
   * 
   * @param container the input container
   * @return the kubernetes model container
   */
  public static Container adapt(io.dekorate.kubernetes.config.Container container) {
    ContainerBuilder builder = new ContainerBuilder();
    applyContainerToBuilder(builder, container);
    return builder.build();
  }
}
