package io.ap4k.component.model;

import io.ap4k.deps.jackson.annotation.JsonIgnore;
import io.ap4k.deps.jackson.annotation.JsonInclude;
import io.ap4k.deps.jackson.annotation.JsonProperty;
import io.ap4k.deps.jackson.annotation.JsonPropertyOrder;
import io.ap4k.deps.jackson.databind.annotation.JsonDeserialize;
import io.ap4k.deps.javax.validation.Valid;
import io.ap4k.deps.javax.validation.constraints.NotNull;
import io.ap4k.deps.kubernetes.api.model.Doneable;
import io.ap4k.deps.kubernetes.api.model.KubernetesResource;
import io.ap4k.deps.kubernetes.api.model.KubernetesResourceList;
import io.ap4k.deps.kubernetes.api.model.ListMeta;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "items"
})
@JsonDeserialize(using = io.ap4k.deps.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.ap4k.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class ComponentList implements KubernetesResource, KubernetesResourceList
{

    /**
     *
     * (Required)
     *
     */
    @NotNull
    @JsonProperty("apiVersion")
    private String apiVersion = "v1";
    /**
     *
     *
     */
    @JsonProperty("items")
    @Valid
    private List<Component> items = new ArrayList<Component>();
    /**
     *
     * (Required)
     *
     */
    @NotNull
    @JsonProperty("kind")
    private String kind = "ComponentList";
    /**
     *
     *
     */
    @JsonProperty("metadata")
    @Valid
    private ListMeta metadata;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


  public ComponentList(String apiVersion, List<Component> items, String kind, ListMeta metadata, Map<String, Object> additionalProperties) {
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
  public List<Component> getItems() {
    return items;
  }

  public void setItems(List<Component> items) {
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
}
