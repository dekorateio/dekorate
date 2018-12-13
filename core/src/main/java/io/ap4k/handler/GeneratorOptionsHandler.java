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
package io.ap4k.handler;

import io.ap4k.Ap4kException;
import io.ap4k.Handler;
import io.ap4k.Resources;
import io.ap4k.config.EditableGeneratorConfig;
import io.ap4k.config.GeneratorConfig;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.KubernetesResource;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.utils.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class GeneratorOptionsHandler implements Handler<GeneratorConfig> {

  protected final Resources resources;
  protected final Function<String, InputStream> read;

  public GeneratorOptionsHandler(Resources resources, Function<String, InputStream> read) {
    this.resources = resources;
    this.read = read;
  }

  @Override
  public int order() {
    return 100;
  }

  @Override
  public void handle(GeneratorConfig config) {
    resources.groups().keySet().forEach(n -> {
      read(n).forEach(i -> resources.add(n, i));
    });
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(GeneratorConfig.class) ||
      type.equals(EditableGeneratorConfig.class);
  }

  /**
   * Red a list of {@link HasMetadata} from the specified resource.
   * @param name  The name of the resource.
   * @return      The list of {@link HasMetadata} or empty list if no resource found.
   */
   protected List<HasMetadata> read(String name) {
    try {
      try (InputStream is = read.apply(name)) {
        if (is == null)  {
          return Collections.emptyList();
        }

        KubernetesResource resource = Serialization.unmarshal(is, KubernetesResource.class);
        if (resource instanceof KubernetesList) {
          return ((KubernetesList) resource).getItems();
        } else if (resource instanceof HasMetadata) {
          return Arrays.asList((HasMetadata)resource);
        } else {
          return Collections.emptyList();
        }
      }
    } catch (IOException e) {
      throw Ap4kException.launderThrowable("Failed ot read resource with name:" + name, e);
    }
  }
}
