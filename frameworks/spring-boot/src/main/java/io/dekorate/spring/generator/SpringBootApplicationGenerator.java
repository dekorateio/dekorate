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

import io.dekorate.Handler;
import io.dekorate.WithSession;
import io.dekorate.Generator;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.ProbeBuilder;
import io.dekorate.kubernetes.decorator.AddLivenessProbeDecorator;
import io.dekorate.kubernetes.decorator.AddReadinessProbeDecorator;
import io.dekorate.kubernetes.decorator.AddRoleBindingDecorator;
import io.dekorate.kubernetes.decorator.AddServiceAccountDecorator;
import io.dekorate.kubernetes.decorator.ApplyServiceAccountDecorator;
import io.dekorate.prometheus.config.EditableServiceMonitorConfig;
import io.dekorate.prometheus.decorator.EndpointPathDecorator;
import io.dekorate.spring.config.SpringApplicationConfig;
import io.dekorate.spring.config.SpringApplicationConfigBuilder;
import io.dekorate.spring.configurator.SetSpringBootRuntime;
import io.dekorate.spring.configurator.SetSpringBootVersion;

import java.util.Collections;
import java.util.Map;

public interface SpringBootApplicationGenerator extends Generator, WithSession {

  Map<String, Object> SPRING_BOOT_APPLICATION = Collections.emptyMap();

  @Override
  default void add(Map map) {
    session.configurators().add(new ConfigurationSupplier(new SpringApplicationConfigBuilder()));
    session.handlers().add(new Handler() {
      @Override
      public int order() {
        return 600;
      }

      @Override
      public void handle(Configuration config) {
        session.configurators().add(new SetSpringBootRuntime());
        session.configurators().add(new SetSpringBootVersion());
      }

      @Override
      public boolean canHandle(Class config) {
        return Configuration.class.isAssignableFrom(config);
      }
    });

    session.handlers().add(new Handler() {
       @Override
       public int order() {
         return 410;
       }

       @Override
       public void handle(Configuration config) {
         session.resources().decorate(new EndpointPathDecorator(session.resources().getName(), "http", "/actuator/prometheus"));
       }

       @Override
       public boolean canHandle(Class config) {
         return SpringApplicationConfig.class.equals(config) || EditableServiceMonitorConfig.class.equals(config);
       }
     });

    if (isActuatorAvailable()) {
      session.handlers().add(new Handler() {
          @Override
           public int order() {
            return 305; //We just want to run right after KubernetesHandler or OpenshiftHanlder.
           }

          @Override
          public void handle(Configuration config) {
            session.resources().decorate(new AddLivenessProbeDecorator(session.resources().getName(), session.resources().getName(), new ProbeBuilder().withHttpActionPath("/actuator/health").build()));
            session.resources().decorate(new AddReadinessProbeDecorator(session.resources().getName(), session.resources().getName(), new ProbeBuilder().withHttpActionPath("/actuator/health").build()));
          }

          @Override
          public boolean canHandle(Class config) {
            return SpringApplicationConfig.class.isAssignableFrom(config);
          }
        });
    }

    if (isSpringCloudKubernetesAvailable()) {
      session.handlers().add(new Handler() {
          @Override
           public int order() {
            return 310; //We just want to run right after KubernetesHandler or OpenshiftHanlder.
           }

          @Override
          public void handle(Configuration config) {
            session.resources().decorate(new ApplyServiceAccountDecorator(session.resources().getName(), session.resources().getName()));
            session.resources().decorate(new AddServiceAccountDecorator(session.resources()));
            session.resources().decorate(new AddRoleBindingDecorator(session.resources(), "view"));
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
