
package io.ap4k.deps.kubernetes.api.model.apiextensions;

import io.ap4k.deps.jackson.annotation.JsonAnyGetter;
import io.ap4k.deps.jackson.annotation.JsonAnySetter;
import io.ap4k.deps.jackson.annotation.JsonIgnore;
import io.ap4k.deps.jackson.annotation.JsonInclude;
import io.ap4k.deps.jackson.annotation.JsonProperty;
import io.ap4k.deps.jackson.annotation.JsonPropertyOrder;
import io.ap4k.deps.jackson.annotation.JsonUnwrapped;
import io.ap4k.deps.jackson.databind.JsonDeserializer;
import io.ap4k.deps.jackson.databind.annotation.JsonDeserialize;
import io.ap4k.deps.javax.validation.Valid;
import io.ap4k.deps.kubernetes.api.model.KubernetesResource;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * We shadow this class, so that we can control serialization.
 * We've added @JsonUnwrapped for schemas and json schemas.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "JSONSchemas",
    "Schema"
})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class JSONSchemaPropsOrArray implements KubernetesResource
{

    /**
     * 
     * 
     */
    @JsonUnwrapped
    @JsonProperty("JSONSchemas")
    @Valid
    private List<JSONSchemaProps> jSONSchemas = new ArrayList<JSONSchemaProps>();
    /**
     * 
     * 
     */
    @JsonUnwrapped
    @JsonProperty("Schema")
    @Valid
    private JSONSchemaProps schema;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public JSONSchemaPropsOrArray() {
    }

    /**
     * 
     * @param schema
     * @param jSONSchemas
     */
    public JSONSchemaPropsOrArray(List<JSONSchemaProps> jSONSchemas, JSONSchemaProps schema) {
        this.jSONSchemas = jSONSchemas;
        this.schema = schema;
    }

    /**
     * 
     * 
     * @return
     *     The jSONSchemas
     */
    @JsonProperty("JSONSchemas")
    public List<JSONSchemaProps> getJSONSchemas() {
        return jSONSchemas;
    }

    /**
     * 
     * 
     * @param jSONSchemas
     *     The JSONSchemas
     */
    @JsonProperty("JSONSchemas")
    public void setJSONSchemas(List<JSONSchemaProps> jSONSchemas) {
        this.jSONSchemas = jSONSchemas;
    }

    /**
     * 
     * 
     * @return
     *     The schema
     */
    @JsonProperty("Schema")
    public JSONSchemaProps getSchema() {
        return schema;
    }

    /**
     * 
     * 
     * @param schema
     *     The Schema
     */
    @JsonProperty("Schema")
    public void setSchema(JSONSchemaProps schema) {
        this.schema = schema;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
