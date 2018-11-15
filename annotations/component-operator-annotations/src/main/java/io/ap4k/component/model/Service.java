package io.ap4k.component.model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.Doneable;
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
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.fabric8.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
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
