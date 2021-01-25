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
package io.dekorate.utils;

import static io.dekorate.utils.Serialization.asYaml;

import io.dekorate.DekorateException;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.lang.reflect.Method;

/*
 * As dekorate is using a shaded version of the kubernetes client, there are cases where we need to convert from the actual to that shaded/internal one.
 * This class provides utility methods for doing that.
 */
public class Clients {

  /*
   * Adapt an existing client instance, to the internal one.
   * 
   * @param instance A client instance.
   * 
   * @return An intneranl KubernetesClient.
   */
  public static KubernetesClient fromInstance(Object client) {
    try {
      Method m = client.getClass().getMethod("getConfiguration");
      return fromConfig(m.invoke(client));
    } catch (Throwable e) {
      throw DekorateException
          .launderThrowable("Type: " + client.getClass() + " is not adaptable to internal Kubernetes Client!", e);
    }
  }

  /*
   * Create an internal client from an external configuration instnace.
   * 
   * @param config A client configuration.
   * 
   * @return An intneranl KubernetesClient.
   */
  public static KubernetesClient fromConfig(Object config) {
    return new DefaultKubernetesClient(unmarshal(asYaml(config), Config.class));
  }
}
