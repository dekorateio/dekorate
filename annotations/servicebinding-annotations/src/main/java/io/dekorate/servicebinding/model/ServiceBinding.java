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

package io.dekorate.servicebinding.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", refs = @BuildableReference(ObjectMeta.class))
@Group("binding.operators.coreos.com")
@Version("v1alpha1")
public class ServiceBinding implements HasMetadata {

  @JsonProperty("kind")
  private String kind = "ServiceBinding";
  @JsonProperty("apiVersion")
  private String apiVersion = "binding.operators.coreos.com/v1alpha1";
  @JsonProperty("metadata")
  private ObjectMeta metadata;
  @JsonProperty("spec")
  private ServiceBindingSpec spec;

  public ServiceBinding() {
  }

  public ServiceBinding(String kind, String apiVersion, ObjectMeta metadata, ServiceBindingSpec spec) {
    this.kind = kind;
    this.apiVersion = apiVersion;
    this.metadata = metadata;
    this.spec = spec;
  }

  @JsonProperty("kind")
  public String getKind() {
    return kind;
  }

  @JsonProperty("kind")
  public void setKind(String kind) {
    this.kind = kind;
  }

  @JsonProperty("apiVersion")
  public String getApiVersion() {
    return apiVersion;
  }

  @JsonProperty("apiVersion")
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  @JsonProperty("metadata")
  public ObjectMeta getMetadata() {
    return metadata;
  }

  @JsonProperty("metadata")
  public void setMetadata(ObjectMeta metadata) {
    this.metadata = metadata;
  }

  @JsonProperty("spec")
  public ServiceBindingSpec getSpec() {
    return spec;
  }

  @JsonProperty("spec")
  public void setSpec(ServiceBindingSpec spec) {
    this.spec = spec;
  }
}
