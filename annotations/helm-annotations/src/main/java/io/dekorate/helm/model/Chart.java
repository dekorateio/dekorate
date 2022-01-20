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
package io.dekorate.helm.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the <a href="https://github.com/helm/helm">Helm</a>
 * <a href="https://github.com/helm/helm/blob/c7e1f9b04606574b2f0d93d34e22efb06847ad08/pkg/chart/metadata.go#L44">Chart.yaml file</a>
 */
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Chart {
  @JsonProperty
  private String apiVersion;
  @JsonProperty
  private String name;
  @JsonProperty
  private String home;
  @JsonProperty
  private List<String> sources;
  @JsonProperty
  private String version;
  @JsonProperty
  private String description;
  @JsonProperty
  private List<String> keywords;
  @JsonProperty
  private List<Maintainer> maintainers;
  @JsonProperty
  private String engine;
  @JsonProperty
  private String icon;
  @JsonProperty
  private List<HelmDependency> dependencies;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<HelmDependency> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<HelmDependency> dependencies) {
    this.dependencies = dependencies;
  }

  public List<Maintainer> getMaintainers() {
    return maintainers;
  }

  public void setMaintainers(List<Maintainer> maintainers) {
    this.maintainers = maintainers;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }

  public List<String> getSources() {
    return sources;
  }

  public void setSources(List<String> sources) {
    this.sources = sources;
  }

  public String getEngine() {
    return engine;
  }

  public void setEngine(String engine) {
    this.engine = engine;
  }

  public String getHome() {
    return home;
  }

  public void setHome(String home) {
    this.home = home;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  @Override
  public String toString() {
    return "Chart{" +
        "name='" + name + '\'' +
        ", home='" + home + '\'' +
        ", version='" + version + '\'' +
        '}';
  }

}
