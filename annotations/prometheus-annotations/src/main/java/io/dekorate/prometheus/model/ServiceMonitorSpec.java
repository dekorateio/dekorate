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
package io.dekorate.prometheus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;
import io.sundr.builder.annotations.Inline;

import java.util.List;

@Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.fabric8.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"), refs = @BuildableReference(ObjectMeta.class))
public class ServiceMonitorSpec {

  @JsonProperty("selector")
  @Valid
  private LabelSelector selector;

  @JsonProperty("endpoints")
  @Valid
  List<Endpoint> endpoints;

  public ServiceMonitorSpec() {
  }

  public ServiceMonitorSpec(LabelSelector selector, List<Endpoint> endpoints) {
    this.selector = selector;
    this.endpoints = endpoints;
  }

  public LabelSelector getSelector() {
    return selector;
  }

  public void setSelector(LabelSelector selector) {
    this.selector = selector;
  }

  public List<Endpoint> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(List<Endpoint> endpoints) {
    this.endpoints = endpoints;
  }
}
