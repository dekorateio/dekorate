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

package io.dekorate.kubernetes.config;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.dekorate.project.BuildInfo;
import io.dekorate.project.Project;
import io.dekorate.utils.Strings;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder", refs = {
    @BuildableReference(Project.class),
    @BuildableReference(BuildInfo.class)
})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class ImageConfiguration extends ApplicationConfiguration {

  private Boolean enabled;

  private String registry;
  private String group;
  private String name;
  private String version;

  private String image;
  private String dockerFile;
  private Boolean autoBuildEnabled;
  private Boolean autoPushEnabled;
  private Boolean autoLoadEnabled;

  public static ImageConfiguration from(ApplicationConfiguration applicationConfiguration) {
    return new ImageConfigurationBuilder()
        .withEnabled(true)
        .withProject(applicationConfiguration.getProject())
        .withGroup(applicationConfiguration.getPartOf())
        .withPartOf(applicationConfiguration.getPartOf())
        .withName(applicationConfiguration.getName())
        .withVersion(applicationConfiguration.getVersion())
        .withAttributes(applicationConfiguration.getAttributes())
        .build();
  }

  public ImageConfiguration() {
  }

  public ImageConfiguration(Project project, Map<ConfigKey, Object> attributes, Boolean enabled, String registry, String group,
      String name, String version, String image, String dockerFile, Boolean autoBuildEnabled, Boolean autoPushEnabled,
      Boolean autoLoadEnabled) {
    super(project, attributes, group, name, version);
    this.enabled = enabled;
    this.registry = registry;
    this.group = Strings.isNotNullOrEmpty(group) ? group : System.getProperty("user.name");
    this.name = name;
    this.version = version;
    this.image = image;
    this.dockerFile = dockerFile;
    this.autoBuildEnabled = autoBuildEnabled;
    this.autoPushEnabled = autoPushEnabled;
    this.autoLoadEnabled = autoLoadEnabled;
  }

  public Boolean getEnabled() {
    return this.enabled;
  }

  public boolean isEnabled() {
    return this.enabled != null && this.enabled;
  }

  public String getRegistry() {
    return registry;
  }

  public void setRegistry(String registry) {
    this.registry = registry;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public String getDockerFile() {
    return dockerFile;
  }

  public void setDockerFile(String dockerFile) {
    this.dockerFile = dockerFile;
  }

  public Boolean getAutoBuildEnabled() {
    return autoBuildEnabled;
  }

  public boolean isAutoBuildEnabled() {
    return autoBuildEnabled != null && autoBuildEnabled;
  }

  public void setAutoBuildEnabled(Boolean autoBuildEnabled) {
    this.autoBuildEnabled = autoBuildEnabled;
  }

  public Boolean getAutoPushEnabled() {
    return autoPushEnabled;
  }

  public boolean isAutoPushEnabled() {
    return autoPushEnabled != null && autoPushEnabled;
  }

  public void setAutoPushEnabled(Boolean autoPushEnabled) {
    this.autoPushEnabled = autoPushEnabled;
  }

  public boolean isAutoLoadEnabled() {
    return autoLoadEnabled != null && autoLoadEnabled;
  }

  public Boolean getAutoLoadEnabled() {
    return autoLoadEnabled;
  }

  public void setAutoLoadEnabled(Boolean autoLoadEnabled) {
    this.autoLoadEnabled = autoLoadEnabled;
  }
}
