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
package io.ap4k.kubernetes;

import io.ap4k.AbstractKubernetesHandler;
import io.ap4k.Resources;
import io.ap4k.config.KubernetesConfig;
import io.ap4k.config.EditableKubernetesConfig;
import io.ap4k.config.Configuration;

public class KubernetesHandler extends AbstractKubernetesHandler<KubernetesConfig> {

  private static final String KUBERNETES = "kubernetes";

  public KubernetesHandler() {
    this(new Resources());
  }

  public KubernetesHandler(Resources resources) {
    super(resources);
  }

  public void handle(KubernetesConfig config) {
    resources.add(KUBERNETES, KubernetesResources.createDeployment(config));
    addVisitors(KUBERNETES, config);
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(KubernetesConfig.class) ||
      type.equals(EditableKubernetesConfig.class);
  }
}
