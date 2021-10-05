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
package io.dekorate.servicebinding.adapter;

import java.util.Arrays;
import java.util.Map;

import io.dekorate.kubernetes.config.Env;
import io.dekorate.servicebinding.annotation.Application;
import io.dekorate.servicebinding.annotation.BindingPath;
import io.dekorate.servicebinding.annotation.ServiceBinding;
import io.dekorate.servicebinding.config.ApplicationConfig;
import io.dekorate.servicebinding.config.ApplicationConfigBuilder;
import io.dekorate.servicebinding.config.BindingPathConfig;
import io.dekorate.servicebinding.config.ServiceBindingConfig;
import io.dekorate.servicebinding.config.ServiceBindingConfigBuilder;
import io.dekorate.servicebinding.config.ServiceConfig;
import io.dekorate.servicebinding.config.ServiceConfigBuilder;

public class ServiceBindingConfigAdapter {

  public static ServiceBindingConfig adapt(ServiceBinding instance) {
    return newBuilder(instance).build();
  }

  public static ServiceBindingConfigBuilder newBuilder(ServiceBinding instance) {
    Application app = instance.application();
    BindingPath bindingPath = instance.bindingPath();
    return new ServiceBindingConfigBuilder(
        new io.dekorate.servicebinding.config.ServiceBindingConfig(null, null, null, instance.name(), null,
            new ApplicationConfigBuilder()
                .withResource(app.resource())
                .withKind(app.kind())
                .withGroup(app.group())
                .withVersion(app.version())
                .withName(app.name())
                .build(),
            Arrays.stream(instance.services())
                .map(s -> new ServiceConfig(null, null, s.group(), s.kind(), s.name(), s.version(), s.id(),
                    s.namespace(), s.envVarPrefix()))
                .toArray(ServiceConfig[]::new),
            instance.envVarPrefix(), instance.detectBindingResources(), instance.bindAsFiles(), instance.mountPath(),
            Arrays.stream(instance.customEnvVar())
                .map(c -> new Env(c.name(), c.value(), c.secret(), c.configmap(), c.field(), c.resourceField()))
                .toArray(Env[]::new),
            new BindingPathConfig(null, null, bindingPath.containerPath(), bindingPath.secretPath())));
  }

  public static ServiceBindingConfig adapt(Map map) {
    return getServiceBindingConfig(map);
  }

  private static ServiceBindingConfig getServiceBindingConfig(Map map) {
    return new ServiceBindingConfig(null, null, null, (String) map.getOrDefault("name", ""), null,
        getApplicationConfig((Map) map.get("application")),
        Arrays.stream((Map[]) map.getOrDefault("services", new Map[0]))
            .map(ServiceBindingConfigAdapter::getServiceConfig).toArray(ServiceConfig[]::new),
        (String) map.getOrDefault("envVarPrefix", ""),
        Boolean.parseBoolean(String.valueOf(map.getOrDefault("detectBindingResources", "false"))),
        Boolean.parseBoolean(String.valueOf(map.getOrDefault("bindAsFiles", "false"))),
        (String) map.getOrDefault("mountPath", ""),
        Arrays.stream((Map[]) map.getOrDefault("customEnvVar", new Map[0]))
            .map(ServiceBindingConfigAdapter::getCustomEnvVarConfig).toArray(Env[]::new),
        getBindingPathConfig((Map) map.get("bindingPath")));
  }

  public static ServiceBindingConfigBuilder newBuilder(Map map) {
    return new ServiceBindingConfigBuilder(getServiceBindingConfig(map));
  }

  private static ApplicationConfig getApplicationConfig(Map i) {
    if (i == null) {
      return null;
    }
    return new ApplicationConfigBuilder()
        .withResource((String) i.getOrDefault("resource", null))
        .withKind((String) i.getOrDefault("kind", null))
        .withGroup((String) i.getOrDefault("group", null))
        .withVersion((String) i.getOrDefault("version", null))
        .withName((String) i.getOrDefault("name", null))
        .build();
  }

  private static ServiceConfig getServiceConfig(Map i) {
    String apiVersion = (String) i.getOrDefault("apiVersion", null);
    String apiVersionGroup = apiVersion != null && apiVersion.contains("/") ? apiVersion.split("/")[0] : "";
    String apiVersionVersion = apiVersion != null && apiVersion.contains("/") ? apiVersion.split("/")[1] : apiVersion;

    return new ServiceConfigBuilder()
        .withKind((String) i.getOrDefault("kind", null))
        .withGroup((String) i.getOrDefault("group", apiVersionGroup))
        .withVersion((String) i.getOrDefault("version", apiVersionVersion))
        .withName((String) i.getOrDefault("name", null))
        .withNamespace((String) i.getOrDefault("namespace", null))
        .withId((String) i.getOrDefault("id", null))
        .withEnvVarPrefix((String) i.getOrDefault("envVarPrefix", null))
        .build();
  }

  private static Env getCustomEnvVarConfig(Map i) {
    return new Env((String) i.getOrDefault("name", null), (String) i.getOrDefault("value", null),
        (String) i.getOrDefault("secret", null), (String) i.getOrDefault("configmap", null),
        (String) i.getOrDefault("field", null), (String) i.getOrDefault("resourceField", null));
  }

  private static BindingPathConfig getBindingPathConfig(Map i) {
    if (i == null) {
      return null;
    }
    return new BindingPathConfig(null, null, (String) i.getOrDefault("containerPath", null),
        (String) i.getOrDefault("secretPath", null));
  }

}
