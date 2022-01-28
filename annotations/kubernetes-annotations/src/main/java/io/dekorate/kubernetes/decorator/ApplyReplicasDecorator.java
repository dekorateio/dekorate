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

import static io.dekorate.ConfigReference.generateConfigReferenceName;

import java.util.Arrays;
import java.util.List;

import io.dekorate.ConfigReference;
import io.dekorate.WithConfigReferences;
import io.dekorate.doc.Description;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecFluent;

@Description("Apply the number of replicas to the DeploymentSpec.")
public class ApplyReplicasDecorator extends NamedResourceDecorator<DeploymentSpecFluent> implements WithConfigReferences {

  private final int replicas;

  public ApplyReplicasDecorator(int replicas) {
    this(ANY, replicas);
  }

  public ApplyReplicasDecorator(String deploymentName, int replicas) {
    super(deploymentName);
    this.replicas = replicas;
  }

  @Override
  public void andThenVisit(DeploymentSpecFluent deploymentSpec, ObjectMeta resourceMeta) {
    if (replicas > 0) {
      deploymentSpec.withReplicas(replicas);
    }
  }

  @Override
  public List<ConfigReference> getConfigReferences() {
    return Arrays.asList(buildConfigReferenceReplicas());
  }

  private ConfigReference buildConfigReferenceReplicas() {
    String property = generateConfigReferenceName("replicas", getName());
    String jsonPath = "$.[?(@.kind == 'Deployment')].spec.replicas";
    if (!Strings.equals(getName(), ANY)) {
      jsonPath = "$.[?(@.kind == 'Deployment' && @.metadata.name == '" + getName() + "')].spec.replicas";
    }

    return new ConfigReference(property, jsonPath);
  }
}
