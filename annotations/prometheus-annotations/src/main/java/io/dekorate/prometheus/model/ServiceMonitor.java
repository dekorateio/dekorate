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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer.None;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

@JsonDeserialize(using = None.class)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "spec",
})
@Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.fabric8.kubernetes.api.builder", refs = {
    @BuildableReference(ObjectMeta.class), @BuildableReference(LabelSelector.class) })
@Group("monitoring.coreos.com")
@Version("v1")
public class ServiceMonitor implements HasMetadata {

  /**
   *
   * (Required)
   *
   */
  @JsonProperty("apiVersion")
  private String apiVersion = "monitoring.coreos.com/v1";
  /**
   *
   * (Required)
   *
   */
  @JsonProperty("kind")
  private String kind = "ServiceMonitor";
  /**
   *
   *
   */
  @JsonProperty("metadata")
  private ObjectMeta metadata;
  /**
   *
   *
   */
  @JsonProperty("spec")
  private ServiceMonitorSpec spec;

  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  public ServiceMonitor() {
  }

  public ServiceMonitor(String apiVersion, String kind, ObjectMeta metadata, ServiceMonitorSpec spec,
      Map<String, Object> additionalProperties) {
    this.apiVersion = apiVersion;
    this.kind = kind;
    this.metadata = metadata;
    this.spec = spec;
    this.additionalProperties = additionalProperties;
  }

  @Override
  public String getApiVersion() {
    return apiVersion;
  }

  @Override
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  @Override
  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  @Override
  public ObjectMeta getMetadata() {
    return metadata;
  }

  @Override
  public void setMetadata(ObjectMeta metadata) {
    this.metadata = metadata;
  }

  public ServiceMonitorSpec getSpec() {
    return spec;
  }

  public void setSpec(ServiceMonitorSpec spec) {
    this.spec = spec;
  }

  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  public void setAdditionalProperties(Map<String, Object> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }
}
