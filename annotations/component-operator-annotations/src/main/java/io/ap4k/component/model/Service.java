package io.ap4k.component.model;
import io.ap4k.deps.jackson.annotation.JsonInclude;
import io.ap4k.deps.jackson.annotation.JsonProperty;
import io.ap4k.deps.jackson.annotation.JsonPropertyOrder;
import io.ap4k.deps.jackson.databind.annotation.JsonDeserialize;
import io.ap4k.deps.kubernetes.api.model.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;
/**
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "name",
  "class",
  "plan",
  "externalId",
  "secretName",
  "parameters",
  "parametersJson",
})
@JsonDeserialize(using = io.ap4k.deps.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.ap4k.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class Service {

  private String name;

  @JsonProperty("class")
  private String serviceClass;
  @JsonProperty("plan")
  private String servicePlan;
  private String externalId;
  private String secretName;
  private Parameter[] parameters;
  private String parametersJson;

  public Service(String name, String serviceClass, String servicePlan, String externalId, String secretName, Parameter[] parameters, String parametersJson) {
    this.name = name;
    this.serviceClass = serviceClass;
    this.servicePlan = servicePlan;
    this.externalId = externalId;
    this.secretName = secretName;
    this.parameters = parameters;
    this.parametersJson = parametersJson;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getServiceClass() {
    return serviceClass;
  }

  public void setServiceClass(String serviceClass) {
    this.serviceClass = serviceClass;
  }

  public String getServicePlan() {
    return servicePlan;
  }

  public void setServicePlan(String servicePlan) {
    this.servicePlan = servicePlan;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getSecretName() {
    return secretName;
  }

  public void setSecretName(String secretName) {
    this.secretName = secretName;
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
