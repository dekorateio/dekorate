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
package io.dekorate.kubernetes.decorator;

import io.dekorate.kubernetes.config.DeploymentStrategy;
import io.dekorate.kubernetes.config.RollingUpdate;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecFluent;

public class ApplyDeploymentStrategyDecorator extends NamedResourceDecorator<DeploymentSpecFluent<?>> {

  private final DeploymentStrategy strategy;
  private final RollingUpdate rollingUpdate;

  public ApplyDeploymentStrategyDecorator(String name, DeploymentStrategy strategy) {
    this(name, strategy, null);
  }

  public ApplyDeploymentStrategyDecorator(String name, DeploymentStrategy strategy, RollingUpdate rollingUpdate) {
    super(name);
    this.strategy = strategy;
    this.rollingUpdate = rollingUpdate;
  }

  @Override
  public void andThenVisit(final DeploymentSpecFluent<?> spec, final ObjectMeta resourceMeta) {
    boolean hasCustomRollingUpdate = hasCusomRollingUpdateConfig(rollingUpdate);
    if (strategy == DeploymentStrategy.Recreate) {
      if (hasCustomRollingUpdate) {
        throw new IllegalStateException(
            "Detected both Recreate strategy and custom Rolling Update config. Please use one or the other!");
      }
      spec.withNewStrategy()
          .withType("Recreate")
          .endStrategy();
    } else if (strategy == DeploymentStrategy.RollingUpdate || hasCustomRollingUpdate) {
      spec.withNewStrategy()
          .withType("RollingUpdate")
          .withNewRollingUpdate()
          .withNewMaxSurge().withValue(rollingUpdate.getMaxSurge()).endMaxSurge()
          .withNewMaxUnavailable().withValue(rollingUpdate.getMaxUnavailable()).endMaxUnavailable()
          .endRollingUpdate()
          .endStrategy();
    }
  }

  private boolean hasCusomRollingUpdateConfig(RollingUpdate rollingUpdate) {
    return rollingUpdate != null &&
        ((rollingUpdate.getMaxUnavailable() != null && !rollingUpdate.getMaxUnavailable().equals("25%")) ||
            (rollingUpdate.getMaxSurge() != null && !rollingUpdate.getMaxSurge().equals("25%")));
  }
}
