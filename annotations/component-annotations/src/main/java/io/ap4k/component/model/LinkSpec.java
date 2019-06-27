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
package io.ap4k.component.model;

import io.ap4k.crd.annotation.CustomResource;
import io.ap4k.deps.jackson.annotation.JsonInclude;
import io.ap4k.deps.jackson.annotation.JsonPropertyOrder;
import io.ap4k.deps.kubernetes.api.model.Doneable;
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
  "kind",
  "ref",
  "envs"
})
@Buildable(editableEnabled = false, builderPackage = "io.ap4k.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
@VelocityTransformations({
  @VelocityTransformation(value = "/component-resource.vm"),
  @VelocityTransformation(value = "/component-resource-list.vm"),
  @VelocityTransformation(value = "/component-status.vm"),
})
@CustomResource(group = "devexp.runtime.redhat.com", version = "v1alpha2")
public class LinkSpec {

  private String componentName;
  private Kind kind;
  private String ref;
  private Env[] envs;

  public LinkSpec() {
  }

  public LinkSpec(String componentName, Kind kind, String ref, Env[] envs) {
    this.componentName = componentName;
    this.kind = kind;
    this.ref = ref;
    this.envs = envs;
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public Kind getKind() {
    return kind;
  }

  public void setKind(Kind kind) {
    this.kind = kind;
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
