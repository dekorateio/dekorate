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

package io.dekorate.config;

import io.dekorate.deps.kubernetes.api.builder.VisitableBuilder;

public class MultiConfiguration<C> extends ConfigurationSupplier<C> {

  public MultiConfiguration() {
    super(null, true);
  }

  public MultiConfiguration(VisitableBuilder<C, ?> builder) {
    super(builder, true);
  }

  @Override
  public int compareTo(ConfigurationSupplier<C> o) {
    if (o instanceof DefaultConfiguration) {
      return 1;
    }
    if (o instanceof PropertyConfiguration) {
      return -1;
    }
    return super.compareTo(o);
  }

}
