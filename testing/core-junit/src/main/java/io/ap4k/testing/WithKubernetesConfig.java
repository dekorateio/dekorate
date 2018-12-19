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
package io.ap4k.testing;

import io.ap4k.Ap4kException;
import io.ap4k.kubernetes.config.KubernetesConfig;
import io.ap4k.project.Project;
import io.ap4k.utils.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface WithKubernetesConfig {

  String KUBERNETES_CONFIG_PATH = "META-INF/ap4k/.config/kubernetes.yml";

  default KubernetesConfig getKubernetesConfig() {
    return getKubernetesConfig(KUBERNETES_CONFIG_PATH);
  }

  default KubernetesConfig getKubernetesConfig (String path) {
    URL url = WithKubernetesConfig.class.getClassLoader().getResource(path);
    if (url != null) {
      try (InputStream is = url.openStream())  {
        return Serialization.unmarshal(is, KubernetesConfig.class);
      } catch (IOException e) {
        throw Ap4kException.launderThrowable(e);
      }
    }
    throw new IllegalStateException("Expected to find kubernetes config at: "+path+"!");
  }
}
