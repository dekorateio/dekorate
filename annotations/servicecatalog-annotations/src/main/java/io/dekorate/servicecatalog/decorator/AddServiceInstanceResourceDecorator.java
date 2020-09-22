/**
 * Copyright 2018 The original authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 
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

package io.dekorate.servicecatalog.decorator;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.servicecatalog.config.Parameter;
import io.dekorate.servicecatalog.config.ServiceCatalogInstance;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.servicecatalog.api.model.ServiceInstanceBuilder;

@Description("Add a ServiceInstance resource(s) to the list of generated resources.")
public class AddServiceInstanceResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private ServiceCatalogInstance instance;

  public AddServiceInstanceResourceDecorator(ServiceCatalogInstance instance) {
    this.instance = instance;
  }

  public void visit(KubernetesListBuilder list) {
    ObjectMeta meta = getMandatoryDeploymentMetadata(list);
    list.addToItems(new ServiceInstanceBuilder()
        .withNewMetadata()
        .withName(meta.getName())
        .endMetadata()
        .withNewSpec()
        .withClusterServiceClassExternalName(instance.getServiceClass())
        .withClusterServicePlanExternalName(instance.getServicePlan())
        .withParameters(toMap(instance.getParameters()))
        .endSpec());
  }

  /**
   * Converts an array of {@link Parameter} to a {@link Map}.
   * 
   * @param parameters The parameters.
   * @return A map.
   */
  protected static Map<String, Object> toMap(Parameter... parameters) {
    return Arrays.asList(parameters).stream().collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
  }
}
