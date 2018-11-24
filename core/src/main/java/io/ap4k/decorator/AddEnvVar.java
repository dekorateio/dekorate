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

package io.ap4k.decorator;

import io.ap4k.config.Env;
import io.ap4k.utils.Strings;
import io.ap4k.deps.kubernetes.api.model.ContainerBuilder;

import java.util.Objects;

/**
 * A decorator that adds an environment variable to all containers.
 */
public class AddEnvVar extends Decorator<ContainerBuilder> {

  private final Env env;

  public AddEnvVar(Env env) {
    this.env = env;
  }

  @Override
  public void visit(ContainerBuilder builder) {
    if (Strings.isNotNullOrEmpty(env.getSecret())) {
      populateFromSecret(builder);
    } else if (Strings.isNotNullOrEmpty(env.getConfigmap())) {
      populateFromConfigMap(builder);
    } else if (Strings.isNotNullOrEmpty(env.getField())) {
      builder.addNewEnv().withName(env.getName()).withNewValueFrom().withNewFieldRef(null, env.getField()).endValueFrom();
    }
    builder.addNewEnv().withName(env.getName()).withValue(env.getValue()).endEnv();
  }


  /**
   * Add an environment variable for the specified envVars.getSecret().
   * If along with the envVars.getSecret() a envVars.getName()/envVars.getValue() has been specified they will be used to create an environment variable
   * name after the envVars.getName(), that will point to the envVars.getSecret() entry named after the envVars.getValue().
   * For example: (envVars.getName()=envVars.getName()1 envVars.getValue()=val1 and envVars.getSecret()=myenv.getSecret()). The added environment variable will be named envVars.getName()1 one
   * and its envVars.getValue() will be read from envVars.getSecret() myenv.getSecret() by getting the envVars.getValue() of the entry named val1.
   *
   * @param builder The builder where the environment variable will be added.
   */
  private void populateFromSecret(ContainerBuilder builder) {
    if (Strings.isNotNullOrEmpty(env.getName()) && Strings.isNotNullOrEmpty(env.getValue())) {
      builder.addNewEnv()
        .withName(env.getName())
        .withNewValueFrom()
        .withNewSecretKeyRef(env.getValue(), env.getSecret(), false)
        .endValueFrom()
        .endEnv();
    } else {
      builder.addNewEnvFrom().withNewSecretRef(env.getSecret(), false).endEnvFrom();
    }
  }



  /**
   * Add an environment variable for the specified config map.
   * If along with the config map a envVars.getName()/envVars.getValue() has been specified they will be used to create an environment variable
   * name after the envVars.getName(), that will point to the envVars.getSecret() entry named after the envVars.getValue().
   * For example: (envVars.getName()=envVars.getName()1 envVars.getValue()=val1 and envVars.getConfigMap()=mymap). The added environment variable will be named envVars.getName()1 one
   * and its envVars.getValue() will be read from configmap mymap by getting the envVars.getValue() of the entry named val1.
   *
   * @param builder The builder where the environment variable will be added.
   */
  private void populateFromConfigMap(ContainerBuilder builder) {
    if (Strings.isNotNullOrEmpty(env.getName()) && Strings.isNotNullOrEmpty(env.getValue())) {
      builder.addNewEnv()
        .withName(env.getName())
        .withNewValueFrom()
        .withNewConfigMapKeyRef(env.getValue(), env.getConfigmap(), false)
        .endValueFrom()
        .endEnv();
    } else {
      builder.addNewEnvFrom().withNewConfigMapRef(env.getConfigmap(), false).endEnvFrom();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AddEnvVar addEnvVar = (AddEnvVar) o;
    return Objects.equals(env, addEnvVar.env);
  }

  @Override
  public int hashCode() {

    return Objects.hash(env);
  }
}
