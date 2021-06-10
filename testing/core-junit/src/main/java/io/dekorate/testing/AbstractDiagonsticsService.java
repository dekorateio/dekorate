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

package io.dekorate.testing;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;

public abstract class AbstractDiagonsticsService<T extends HasMetadata> implements DiagnosticsService<T>, WithKubernetesClient {

  protected final Logger LOGGER = LoggerFactory.getLogger();

  private final KubernetesClient client;

  public AbstractDiagonsticsService(KubernetesClient client) {
    this.client = client;
  }

  @Override
  public KubernetesClient getKubernetesClient() {
    return this.client;
  }
}
