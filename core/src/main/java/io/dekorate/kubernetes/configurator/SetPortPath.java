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
package io.dekorate.kubernetes.configurator;

import java.util.function.Predicate;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.BaseConfigFluent;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.PortBuilder;

@Description("Sets the path for the matching port.")
public class SetPortPath extends Configurator<BaseConfigFluent<?>> {

  private final Predicate<PortBuilder> predicate;
  private final String path;

  public SetPortPath(String name, String path) {
    this((Predicate<PortBuilder>) p -> p.getName().equals(name), path);
  }

  public SetPortPath(Predicate<PortBuilder> predicate, String path) {
    this.predicate = predicate;
    this.path = path;
  }

  @Override
  public void visit(BaseConfigFluent<?> config) {
    if (config.hasMatchingPort(predicate)) {
      config.editMatchingPort(predicate).withPath(path);
    }
  }
}
