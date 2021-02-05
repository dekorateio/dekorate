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

import io.dekorate.kubernetes.config.Configuration;
import io.fabric8.kubernetes.api.builder.VisitableBuilder;

/**
 * A {@link Configuration} {@link Supplier} that can be present multiple times.
 * All suppliers are grouped and merged by type. So, typically we end up with a single property configuration, annotatin configuration etc.
 * MultiConfiguration is the exception to the rule above and is not merged, allowing for multiple MultiConfiguration instances to be passed around.
 * Usually, a MutliConfiguration is used when we have multiple sources of configuration of the same kind, that doesn make sense to be merged together (e.g. multiple annotations). 
 */
public class MultiConfiguration<C extends Configuration> extends ConfigurationSupplier<C> {

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
