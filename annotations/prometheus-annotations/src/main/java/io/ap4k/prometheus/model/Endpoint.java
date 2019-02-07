package io.ap4k.prometheus.model;

import io.ap4k.deps.jackson.annotation.JsonProperty;
import io.ap4k.deps.kubernetes.api.model.Doneable;
import io.ap4k.deps.kubernetes.api.model.ObjectMeta;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;
import io.sundr.builder.annotations.Inline;

@Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.ap4k.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"), refs = @BuildableReference(ObjectMeta.class))
public class Endpoint {

  @JsonProperty("port")
  String port;

  @JsonProperty("path")
  String path;

  @JsonProperty("interval")
  String interval;

  @JsonProperty("honorLabels")
  boolean honorLabels;

  public Endpoint(String port, String path, String interval, boolean honorLabels) {
    this.port = port;
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
