/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.halkyon.model;

import io.dekorate.crd.annotation.CustomResource;
import io.dekorate.deps.jackson.annotation.JsonInclude;
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
  "category",
  "kind",
  "version",
  "parameters",
  "parametersJson",
})
@JsonDeserialize(using = io.dekorate.deps.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.dekorate.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
@VelocityTransformations({
  @VelocityTransformation(value = "/halkyon-resource.vm"),
  @VelocityTransformation(value = "/halkyon-resource-list.vm"),
  @VelocityTransformation(value = "/halkyon-status.vm"),
})
@CustomResource(group = "halkyon.io", version = "v1beta1")
public class CapabilitySpec {
  
  private String category;
  private String kind;
  private String version;
  private Parameter[] parameters;
  private String parametersJson;
  
  public CapabilitySpec() {
  }
  
  public CapabilitySpec(String category, String kind, Parameter[] parameters, String parametersJson) {
    this.category = category;
    this.kind = kind;
    this.version = version;
    this.parameters = parameters;
    this.parametersJson = parametersJson;
  }
  
  public String getCategory() {
    return this.category;
  }
  
  public void setCategory(String category) {
    this.category = category;
  }
  
  public String getKind() {
    return this.kind;
  }
  
  public void setKind(String kind) {
    this.kind = kind;
  }
  
  public String getVersion() {
    return this.version;
  }
  
  public void setVersion(String version) {
    this.version = version;
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
