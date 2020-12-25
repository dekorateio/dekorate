/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.halkyon.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.dekorate.halkyon.annotation.HalkyonComponent;
import io.dekorate.halkyon.config.CapabilityConfig;
import io.dekorate.halkyon.config.ComponentConfig;
import io.dekorate.halkyon.config.ComponentConfigBuilder;
import io.dekorate.halkyon.config.Parameter;
import io.dekorate.halkyon.config.RequiredCapabilityConfig;
import io.dekorate.halkyon.model.DeploymentMode;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.kubernetes.config.Label;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class ComponentConfigAdapter {

  public static ComponentConfig adapt(HalkyonComponent instance) {
    return newBuilder(instance).build();
  }

  public static ComponentConfigBuilder newBuilder(HalkyonComponent instance) {
    return new ComponentConfigBuilder(new io.dekorate.halkyon.config.ComponentConfig(null,
        null,
        instance.partOf(),
        instance.name(),
        instance.version(),
        instance.deploymentMode(),
        instance.exposeService(),
        Arrays.asList(instance.envs()).stream().map(i -> new io.dekorate.kubernetes.config.Env(i.name(),
            i.value(),
            i.secret(),
            i.configmap(),
            i.field(), i.resourceField())).collect(Collectors.toList()).toArray(new io.dekorate.kubernetes.config.Env[0]),
                                                                                     Arrays.asList(instance.labels()).stream().map(i -> new io.dekorate.kubernetes.config.Label(i.key(), i.value(),
                                                                                                                                                                                i.kinds())).collect(Collectors.toList()).toArray(new io.dekorate.kubernetes.config.Label[0]),
        instance.buildType(),
        instance.remote(),
        Arrays.asList(instance.provides()).stream().map(i -> new io.dekorate.halkyon.config.CapabilityConfig(null,
            null,
            i.category(),
            i.type(),
            i.name(),
            i.version(),
            Arrays.asList(i.parameters()).stream().map(j -> new io.dekorate.halkyon.config.Parameter(j.name(),
                j.value())).collect(Collectors.toList()).toArray(new io.dekorate.halkyon.config.Parameter[0])))
            .collect(Collectors.toList()).toArray(new io.dekorate.halkyon.config.CapabilityConfig[0]),
        Arrays.asList(instance.requires()).stream().map(i -> new io.dekorate.halkyon.config.RequiredCapabilityConfig(
            null,
            null,
            i.name(),
            i.category(),
            i.type(),
            i.boundTo(),
            i.autoBindable(),
            Arrays.asList(i.parameters()).stream().map(j -> new io.dekorate.halkyon.config.Parameter(j.name(),
                j.value())).collect(Collectors.toList()).toArray(new io.dekorate.halkyon.config.Parameter[0])))
            .collect(Collectors.toList()).toArray(new io.dekorate.halkyon.config.RequiredCapabilityConfig[0])));
  }

  public static ComponentConfig adapt(Map map) {
    return getComponentConfig(map);
  }

  private static ComponentConfig getComponentConfig(Map map) {
    return new ComponentConfig(
        null,
        null,
        (String) map.getOrDefault("partOf", ""),
        (String) map.getOrDefault("name", ""),
        (String) map.getOrDefault("version", ""),
        DeploymentMode.valueOf((String) map.getOrDefault("deploymentMode", "dev")),
        getBooleanFromStringOrBool("exposeService", map),
        Arrays.stream((Map[]) map.getOrDefault("envs", new Map[0])).map(ComponentConfigAdapter::getEnv)
            .toArray(Env[]::new),
        Arrays.stream((Map[]) map.getOrDefault("labels", new Map[0])).map(ComponentConfigAdapter::getLabel)
            .toArray(Label[]::new),
        (String) map.getOrDefault("buildType", "s2i"),
        (String) map.getOrDefault("remote", "origin"),
        Arrays.stream((Map[]) map.getOrDefault("provides", new Map[0]))
            .map(ComponentConfigAdapter::getProvidedCapability).toArray(CapabilityConfig[]::new),
        Arrays.stream((Map[]) map.getOrDefault("requires", new Map[0]))
            .map(ComponentConfigAdapter::getRequiredCapability).toArray(RequiredCapabilityConfig[]::new));
  }

  private static Parameter[] getParameters(Map i) {
    final Object params = i.get("parameters");
    if (params instanceof List) {
      return ((List<Map>) params).stream().map(ComponentConfigAdapter::getParameter).toArray(Parameter[]::new);
    } else if (params instanceof Map[]) {
      return Arrays.stream((Map[]) params).map(ComponentConfigAdapter::getParameter).toArray(Parameter[]::new);
    }
    return new Parameter[0];
  }

  private static Parameter getParameter(Map j) {
    return new Parameter((String) j.getOrDefault("name", null), (String) j.getOrDefault("value", null));
  }

  private static Label getLabel(Map i) {
    return new Label(
        (String) i.getOrDefault("key", null),
        (String) i.getOrDefault("value", null),
        (String[]) i.getOrDefault("kinds", new String[0]));
  }

  private static Env getEnv(Map i) {
    return new Env(
        (String) i.getOrDefault("name", null),
        (String) i.getOrDefault("value", ""),
        (String) i.getOrDefault("secret", ""),
        (String) i.getOrDefault("configmap", ""),
        (String) i.getOrDefault("field", ""),
        (String) i.getOrDefault("resourceField", ""));
  }

  public static ComponentConfigBuilder newBuilder(Map map) {
    return new ComponentConfigBuilder(getComponentConfig(map));
  }

  private static RequiredCapabilityConfig getRequiredCapability(Map i) {
    final CapabilityConfig config = getProvidedCapability(i);
    return new RequiredCapabilityConfig(
        config.getProject(),
        config.getAttributes(),
        config.getName(),
        config.getCategory(),
        config.getType(),
        (String) i.getOrDefault("boundTo", ""),
        getBooleanFromStringOrBool("autoBindable", i),
        config.getParameters());
  }

  private static boolean getBooleanFromStringOrBool(String key, Map i) {
    return Boolean.parseBoolean(String.valueOf(i.getOrDefault(key, null)));
  }

  private static CapabilityConfig getProvidedCapability(Map i) {
    return new CapabilityConfig(
        null,
        null,
        (String) i.getOrDefault("category", null),
        (String) i.getOrDefault("type", null),
        (String) i.getOrDefault("name", null),
        (String) i.getOrDefault("version", ""),
        getParameters(i));
  }
}
