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
package io.dekorate.openshift.decorator;

import io.dekorate.deps.openshift.api.model.DeploymentConfigSpecFluent;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.Decorator;

@Description("Apply the number of replicas to the DeploymentConfigSpec.")
public class ApplyReplicasDecorator extends Decorator<DeploymentConfigSpecFluent> {

  private final int replicas;

  public ApplyReplicasDecorator(int replicas) {
    this.replicas = replicas;
  }

  @Override
  public void visit(DeploymentConfigSpecFluent deploymentSpec) {
   deploymentSpec.withReplicas(replicas);
  }
}
