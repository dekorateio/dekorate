package io.ap4k.prometheus.model;

import io.ap4k.deps.jackson.annotation.JsonIgnore;
import io.ap4k.deps.jackson.annotation.JsonInclude;
import io.ap4k.deps.jackson.annotation.JsonProperty;
import io.ap4k.deps.jackson.annotation.JsonPropertyOrder;
import io.ap4k.deps.jackson.annotation.ObjectIdGenerators;
import io.ap4k.deps.jackson.databind.JsonDeserializer;
import io.ap4k.deps.jackson.databind.annotation.JsonDeserialize;
import io.ap4k.deps.javax.validation.Valid;
import io.ap4k.deps.javax.validation.constraints.NotNull;
import io.ap4k.deps.kubernetes.api.model.KubernetesResource;
import io.ap4k.deps.kubernetes.api.model.KubernetesResourceList;
import io.ap4k.deps.kubernetes.api.model.ListMeta;

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
