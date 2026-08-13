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

package io.dekorate.prometheus.decorator;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.prometheus.config.ServiceMonitorConfig;
import io.dekorate.prometheus.model.ServiceMonitorBuilder;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;

@Description("Add a ServiceMonitor resource to the list of generated resources.")
public class AddServiceMonitorResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private final ServiceMonitorConfig config;

  public AddServiceMonitorResourceDecorator(ServiceMonitorConfig config) {
    this.config = config;
  }

  public void visit(KubernetesListBuilder list) {
    ObjectMeta meta = getMandatoryDeploymentMetadata(list, ANY);
    list.addToItems(new ServiceMonitorBuilder()
        .withNewMetadata()
        .withName(meta.getName())
        .withLabels(meta.getLabels())
        .endMetadata()
        .withNewSpec()
        .withNewSelector()
        .addToMatchLabels(meta.getLabels())
        .endSelector()
        .addNewEndpoint()
        .withPort(config.getPort())
        .withPath(config.getPath())
        .withInterval(config.getInterval() + "s")
        .withHonorLabels(config.isHonorLabels())
        .endEndpoint()
        .endSpec());
  }

}
