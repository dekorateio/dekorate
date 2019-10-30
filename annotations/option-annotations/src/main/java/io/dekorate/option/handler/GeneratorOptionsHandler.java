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
package io.dekorate.option.handler;

import io.dekorate.DekorateException;
import io.dekorate.Resources;
import io.dekorate.Handler;
import io.dekorate.option.config.EditableGeneratorConfig;
import io.dekorate.option.config.GeneratorConfig;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.api.model.KubernetesResource;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.utils.Serialization;

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

        KubernetesList list = Serialization.unmarshalAsList(is);
        return list.getItems();
      }
    } catch (IOException e) {
      throw DekorateException.launderThrowable("Failed ot read resource with name:" + name, e);
    }
  }
}
