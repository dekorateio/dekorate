/**
 * Copyright (C) 2020 Original Authors
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.dekorate;

import io.dekorate.kubernetes.config.BaseConfig;
import io.fabric8.kubernetes.api.model.HasMetadata;

/**
 * A factory for creating deployment resources.
 */
public interface ResourceFactory {

  String kind();

  HasMetadata create(AbstractKubernetesManifestGenerator<?> generator, BaseConfig config);

}
