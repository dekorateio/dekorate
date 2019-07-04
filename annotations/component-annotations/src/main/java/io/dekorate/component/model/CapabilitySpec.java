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
package io.dekorate.component.model;

import io.dekorate.crd.annotation.CustomResource;
import io.dekorate.deps.jackson.annotation.JsonInclude;
import io.dekorate.deps.jackson.annotation.JsonProperty;
import io.dekorate.deps.jackson.annotation.JsonPropertyOrder;
import io.dekorate.deps.jackson.databind.annotation.JsonDeserialize;
import io.dekorate.deps.kubernetes.api.model.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;
import io.sundr.transform.annotations.VelocityTransformation;
import io.sundr.transform.annotations.VelocityTransformations;

/**
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "class",
  "plan",
  "externalId",
  "secretName",
  "parameters",
  "parametersJson",
})
@JsonDeserialize(using = io.dekorate.deps.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.dekorate.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
@VelocityTransformations({
  @VelocityTransformation(value = "/component-resource.vm"),
  @VelocityTransformation(value = "/component-resource-list.vm"),
  @VelocityTransformation(value = "/component-status.vm"),
})
@CustomResource(group = "devexp.runtime.redhat.com", version = "v1alpha2")
public class CapabilitySpec {

  @JsonProperty("class")
  private String serviceClass;
  @JsonProperty("plan")
  private String servicePlan;
  private String externalId;
  private String secretName;
  private Parameter[] parameters;
  private String parametersJson;

  public CapabilitySpec() {
  }

  public CapabilitySpec(String serviceClass, String servicePlan, String externalId, String secretName, Parameter[] parameters, String parametersJson) {
    this.serviceClass = serviceClass;
    this.servicePlan = servicePlan;
    this.externalId = externalId;
    this.secretName = secretName;
    this.parameters = parameters;
    this.parametersJson = parametersJson;
  }

  public String getServiceClass() {
    return serviceClass;
  }

  public void setServiceClass(String serviceClass) {
    this.serviceClass = serviceClass;
  }

  public String getServicePlan() {
    return servicePlan;
  }

  public void setServicePlan(String servicePlan) {
    this.servicePlan = servicePlan;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getSecretName() {
    return secretName;
  }

  public void setSecretName(String secretName) {
    this.secretName = secretName;
  }

  public Parameter[] getParameters() {
    return parameters;
  }

  public void setParameters(Parameter[] parameters) {
    this.parameters = parameters;
  }

  public String getParametersJson() {
    return parametersJson;
  }

  public void setParametersJson(String parametersJson) {
    this.parametersJson = parametersJson;
  }
}
