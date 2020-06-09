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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ListMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"apiVersion", "items", "kind", "metadata"})
@JsonDeserialize(
    using = JsonDeserializer.None.class
)
public class ServiceMonitorList implements KubernetesResource, KubernetesResourceList {

    @NotNull
    @JsonProperty("apiVersion")
    private String apiVersion = "app.k8s.io/v1beta1";
    @JsonProperty("items")
    @Valid
    private List<ServiceMonitor> items = new ArrayList();
    @NotNull
    @JsonProperty("kind")
    private String kind = "ServiceMonitorList";
    @JsonProperty("metadata")
    @Valid
    private ListMeta metadata;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap();

  public ServiceMonitorList() {
  }

  public ServiceMonitorList(String apiVersion, List<ServiceMonitor> items, String kind, ListMeta metadata, Map<String, Object> additionalProperties) {
    this.apiVersion = apiVersion;
    this.items = items;
    this.kind = kind;
    this.metadata = metadata;
    this.additionalProperties = additionalProperties;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  @Override
  public List<ServiceMonitor> getItems() {
    return items;
  }

  public void setItems(List<ServiceMonitor> items) {
    this.items = items;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  @Override
  public ListMeta getMetadata() {
    return metadata;
  }

  public void setMetadata(ListMeta metadata) {
    this.metadata = metadata;
  }

  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  public void setAdditionalProperties(Map<String, Object> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ServiceMonitorList that = (ServiceMonitorList) o;
    return Objects.equals(apiVersion, that.apiVersion) &&
      Objects.equals(items, that.items) &&
      Objects.equals(kind, that.kind) &&
      Objects.equals(metadata, that.metadata) &&
      Objects.equals(additionalProperties, that.additionalProperties);
  }

  @Override
  public int hashCode() {

    return Objects.hash(apiVersion, items, kind, metadata, additionalProperties);
  }
}
