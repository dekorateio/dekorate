package io.dekorate.helm.util;

import io.dekorate.ConfigReference;

public class HelmValueHolder {

  public final Object value;
  public final ConfigReference configReference;

  public HelmValueHolder(Object value, ConfigReference configReference) {
    this.value = value;
    this.configReference = configReference;
  }
}
