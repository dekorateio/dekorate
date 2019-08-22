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
  "componentName",
  "type",
  "ref",
  "envs"
})
@Buildable(editableEnabled = false, builderPackage = "io.dekorate.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
@VelocityTransformations({
  @VelocityTransformation(value = "/halkyon-resource.vm"),
  @VelocityTransformation(value = "/halkyon-resource-list.vm"),
  @VelocityTransformation(value = "/halkyon-status.vm"),
})
@CustomResource(group = "halkyon.io", version = "v1beta1")
public class LinkSpec {
  
  private String componentName;
  private Type type;
  private String ref;
  private Env[] envs;
  
  public LinkSpec() {
  }
  
  public LinkSpec(String componentName, Type type, String ref, Env[] envs) {
    this.componentName = componentName;
    this.type = type;
    this.ref = ref;
    this.envs = envs;
  }
  
  public String getComponentName() {
    return componentName;
  }
  
  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }
  
  public Type getType() {
    return type;
  }
  
  public void setType(Type type) {
    this.type = type;
  }
  
  public String getRef() {
    return ref;
  }
  
  public void setRef(String ref) {
    this.ref = ref;
  }
  
  public Env[] getEnvs() {
    return envs;
  }
  
  public void setEnvs(Env[] envs) {
    this.envs = envs;
  }
}
