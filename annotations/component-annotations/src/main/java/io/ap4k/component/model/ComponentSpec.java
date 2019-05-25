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
 **/
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
  "deploymentMode",
  "runtime",
  "version",
  "exposeService",
  "storage",
  "envs"
})
@Buildable(editableEnabled = false, builderPackage = "io.ap4k.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
@VelocityTransformations({
  @VelocityTransformation(value = "/component-resource.vm"),
  @VelocityTransformation(value = "/component-resource-list.vm"),
  @VelocityTransformation(value = "/component-status.vm"),
})
@CustomResource(group = "devexp.runtime.redhat.com", version = "v1alpha2")
public class ComponentSpec {

  private DeploymentMode deploymentMode;
  private String runtime;
  private String version;
  private boolean exposeService;
  private Storage storage;
  private Env[] envs;

  public ComponentSpec() {
  }

  public ComponentSpec(DeploymentMode deploymentMode, String runtime, String version, boolean exposeService, Storage storage, Env[] envs) {
    this.deploymentMode = deploymentMode;
    this.runtime = runtime;
    this.version = version;
    this.exposeService = exposeService;
    this.storage = storage;
    this.envs = envs;
  }

  public DeploymentMode getDeploymentMode() {
    return deploymentMode;
  }

  public void setDeploymentMode(DeploymentMode deploymentMode) {
    this.deploymentMode = deploymentMode;
  }

  public String getRuntime() {
    return runtime;
  }

  public void setRuntime(String runtime) {
    this.runtime = runtime;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public boolean isExposeService() {
    return exposeService;
  }

  public void setExposeService(boolean exposeService) {
    this.exposeService = exposeService;
  }

  public Storage getStorage() {
    return storage;
  }

  public void setStorage(Storage storage) {
    this.storage = storage;
  }

  public Env[] getEnvs() {
    return envs;
  }

  public void setEnvs(Env[] envs) {
    this.envs = envs;
  }
}
