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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer.None;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

@JsonDeserialize(using = None.class)
@JsonInclude(Include.NON_NULL)
@Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.fabric8.kubernetes.api.builder", refs = @BuildableReference(ObjectMeta.class))
public class Endpoint {

  @JsonProperty("port")
  String port;

  @JsonProperty("proxyUrl")
  String proxyUrl;

  @JsonProperty("scheme")
  String scheme;

  @JsonProperty("targetPort")
  IntOrString targetPort;

  @JsonProperty("path")
  String path;

  @JsonProperty("interval")
  String interval;

  @JsonProperty("honorLabels")
  boolean honorLabels;

  public Endpoint() {
  }

  public Endpoint(String port, String proxyUrl, String scheme, IntOrString targetPort, String path, String interval,
      boolean honorLabels) {
    this.port = port;
    this.proxyUrl = proxyUrl;
    this.scheme = scheme;
    this.targetPort = targetPort;
    this.path = path;
    this.interval = interval;
    this.honorLabels = honorLabels;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getProxyUrl() {
    return proxyUrl;
  }

  public void setProxyUrl(String proxyUrl) {
    this.proxyUrl = proxyUrl;
  }

  public String getScheme() {
    return scheme;
  }

  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  public IntOrString getTargetPort() {
    return targetPort;
  }

  public void setTargetPort(IntOrString targetPort) {
    this.targetPort = targetPort;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getInterval() {
    return interval;
  }

  public void setInterval(String interval) {
    this.interval = interval;
  }

  public boolean isHonorLabels() {
    return honorLabels;
  }

  public void setHonorLabels(boolean honorLabels) {
    this.honorLabels = honorLabels;
  }
}
