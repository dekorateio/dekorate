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
package io.dekorate.kubernetes.decorator;

import java.util.Objects;
import java.util.function.Predicate;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvFromSourceBuilder;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;

/**
 * A decorator that adds an environment variable to the specified container(s).
 */
@Description("Add a environment variable to the container.")
public class AddEnvVarDecorator extends ApplicationContainerDecorator<ContainerBuilder> {

  private final Env env;

  public AddEnvVarDecorator(Env env) {
    this(ANY, ANY, env);
  }

  public AddEnvVarDecorator(String deployment, String container, Env env) {
    super(deployment, container);
    this.env = env;
  }

  @Override
  public void andThenVisit(ContainerBuilder builder) {
    Predicate<EnvVarBuilder> matchingEnv = new Predicate<EnvVarBuilder>() {
      public boolean test(EnvVarBuilder e) {
        if (e.getName() != null) {
          return e.getName().equals(env.getName());
        }
        return false;
      }
    };

    Predicate<EnvFromSourceBuilder> matchingEnvFrom = new Predicate<EnvFromSourceBuilder>() {
      public boolean test(EnvFromSourceBuilder e) {
        if (e.getSecretRef() != null && e.getSecretRef().getName() != null) {
          return e.getSecretRef().getName().equals(env.getSecret());
        } else if (e.getConfigMapRef() != null && e.editConfigMapRef().getName() != null) {
          return e.editConfigMapRef().getName().equals(env.getConfigmap());
        }
        return false;
      }
    };

    builder.removeMatchingFromEnv(matchingEnv);
    builder.removeMatchingFromEnvFrom(matchingEnvFrom);

    if (Strings.isNotNullOrEmpty(env.getSecret())) {
      populateFromSecret(builder);
    } else if (Strings.isNotNullOrEmpty(env.getConfigmap())) {
      populateFromConfigMap(builder);
    } else if (Strings.isNotNullOrEmpty(env.getField())) {
      populateFromField(builder);
    } else if (Strings.isNotNullOrEmpty(env.getName())) {
      builder.addNewEnv().withName(env.getName()).withValue(env.getValue()).endEnv();
    } else if (Strings.isNotNullOrEmpty(env.getResourceField())) {
      populateFromResourceField(builder);
    }
  }

  /**
   * Add an environment variable for the specified envVars.getSecret(). If along
   * with the envVars.getSecret() a envVars.getName()/envVars.getValue() has been
   * specified they will be used to create an environment variable name after the
   * envVars.getName(), that will point to the envVars.getSecret() entry named
   * after the envVars.getValue().
   *
   * @param builder The builder where the environment variable will be added.
   */
  private void populateFromSecret(ContainerBuilder builder) {
    if (Strings.isNotNullOrEmpty(env.getName()) && Strings.isNotNullOrEmpty(env.getValue())) {
      builder.addNewEnv().withName(env.getName()).withNewValueFrom()
          .withNewSecretKeyRef(env.getValue(), env.getSecret(), null).endValueFrom().endEnv();
    } else {
      builder.addNewEnvFrom().withNewSecretRef(env.getSecret(), null).endEnvFrom();
    }
  }

  /**
   * Add an environment variable for the specified config map. If along with the
   * config map a envVars.getName()/envVars.getValue() has been specified they
   * will be used to create an environment variable name after the
   * envVars.getName(), that will point to the envVars.getSecret() entry named
   * after the envVars.getValue().
   *
   * @param builder The builder where the environment variable will be added.
   */
  private void populateFromConfigMap(ContainerBuilder builder) {
    if (Strings.isNotNullOrEmpty(env.getName()) && Strings.isNotNullOrEmpty(env.getValue())) {
      builder.addNewEnv().withName(env.getName()).withNewValueFrom()
          .withNewConfigMapKeyRef(env.getValue(), env.getConfigmap(), null).endValueFrom().endEnv();
    } else {
      builder.addNewEnvFrom().withNewConfigMapRef(env.getConfigmap(), null).endEnvFrom();
    }
  }

  private void populateFromField(ContainerBuilder builder) {
    builder.addNewEnv().withName(this.env.getName()).withNewValueFrom().withNewFieldRef()
        .withFieldPath(this.env.getField()).endFieldRef().endValueFrom().endEnv();
  }

  private void populateFromResourceField(ContainerBuilder builder) {
    builder.addNewEnv().withName(this.env.getName()).withNewValueFrom().withNewResourceFieldRef()
        .withResource(this.env.getResourceField()).endResourceFieldRef().endValueFrom().endEnv();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AddEnvVarDecorator addEnvVarDecorator = (AddEnvVarDecorator) o;
    return Objects.equals(env, addEnvVarDecorator.env);
  }

  @Override
  public int hashCode() {

    return Objects.hash(env);
  }

  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, ApplyApplicationContainerDecorator.class,
        AddSidecarDecorator.class };
  }

}
