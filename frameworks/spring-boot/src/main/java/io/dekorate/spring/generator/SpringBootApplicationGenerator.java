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
package io.dekorate.spring.generator;

import java.util.Collections;
import java.util.Map;

import io.dekorate.ConfigurationGenerator;
import io.dekorate.ManifestGenerator;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Session;
import io.dekorate.WithSession;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.ProbeBuilder;
import io.dekorate.kubernetes.configurator.AddLivenessProbeConfigurator;
import io.dekorate.kubernetes.configurator.AddReadinessProbeConfigurator;
import io.dekorate.kubernetes.decorator.AddRoleBindingResourceDecorator;
import io.dekorate.kubernetes.decorator.AddServiceAccountResourceDecorator;
import io.dekorate.kubernetes.decorator.ApplyServiceAccountNamedDecorator;
import io.dekorate.prometheus.config.EditableServiceMonitorConfig;
import io.dekorate.prometheus.decorator.EndpointPathDecorator;
import io.dekorate.spring.config.SpringApplicationConfig;
import io.dekorate.spring.config.SpringApplicationConfigBuilder;
import io.dekorate.spring.configurator.SetSpringBootRuntime;
import io.dekorate.spring.configurator.SetSpringBootVersion;

public interface SpringBootApplicationGenerator extends ConfigurationGenerator, WithSession {

  Map<String, Object> SPRING_BOOT_APPLICATION = Collections.emptyMap();
  Logger LOGGER = LoggerFactory.getLogger();

  @Override
  default void addAnnotationConfiguration(Map map) {
    addConfiguration(map);
  }

  @Override
  default void addPropertyConfiguration(Map map) {
    addConfiguration(map);
  }

  default void addConfiguration(Map map) {
    Session session = getSession();
    session.getConfigurationRegistry().add(new ConfigurationSupplier(new SpringApplicationConfigBuilder()));
    session.getConfigurationRegistry().add(new SetSpringBootRuntime());
    session.getConfigurationRegistry().add(new SetSpringBootVersion());

    session.getHandlers().add(new ManifestGenerator() {

      @Override
      public int order() {
        return 410;
      }

      @Override
      public String getKey() {
        return "spring";
      }

      @Override
      public void handle(Configuration config) {
        LOGGER.info("Processing service monitor config.");
        session.getResourceRegistry().decorate(new EndpointPathDecorator("http", "/actuator/prometheus"));
      }

      @Override
      public boolean canHandle(Class config) {
        return SpringApplicationConfig.class.equals(config) || EditableServiceMonitorConfig.class.equals(config);
      }
    });

    if (isActuatorAvailable()) {
      //Users configuration should take priority, so add but don't overwrite.
      session.getConfigurationRegistry().add(
          new AddLivenessProbeConfigurator(new ProbeBuilder().withHttpActionPath("/actuator/info").build(), false));
      session.getConfigurationRegistry().add(new AddReadinessProbeConfigurator(
          new ProbeBuilder().withHttpActionPath("/actuator/health").build(), false));
    }

    if (isSpringCloudKubernetesAvailable()) {
      session.getHandlers().add(new ManifestGenerator() {
        @Override
        public int order() {
          return 310; //We just want to run right after KubernetesHandler or OpenshiftHanlder.
        }

        @Override
        public String getKey() {
          return "spring";
        }

        @Override
        public void handle(Configuration config) {
          LOGGER.info("Detected spring cloud kubernetes.");
          session.getResourceRegistry().decorate(new ApplyServiceAccountNamedDecorator());
          session.getResourceRegistry().decorate(new AddServiceAccountResourceDecorator());
          session.getResourceRegistry().decorate(new AddRoleBindingResourceDecorator("view"));
        }

        @Override
        public boolean canHandle(Class config) {
          return SpringApplicationConfig.class.isAssignableFrom(config);
        }
      });
    }
  }

  default boolean isActuatorAvailable() {
    try {
      Class c = Class.forName("org.springframework.boot.actuate.health.HealthIndicator");
      return c != null;
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      return false;
    }
  }

  default boolean isSpringCloudKubernetesAvailable() {
    try {
      Class c = Class.forName("org.springframework.cloud.kubernetes.KubernetesAutoConfiguration");
      return c != null;
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      return false;
    }
  }
}
