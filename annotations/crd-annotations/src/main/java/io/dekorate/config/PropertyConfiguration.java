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

import io.fabric8.kubernetes.api.builder.VisitableBuilder;

/**
 * This is a {@link ConfigurationSupplier} that is used to wrap property files config.
 * This kind of configuration takes precedence over {@link AnnotationConfiguration} and {@link DefaultConfiguration}.
 */
public class PropertyConfiguration<C> extends ConfigurationSupplier<C> {

  public PropertyConfiguration() {
    super(null, true);
  }

  public PropertyConfiguration(VisitableBuilder<C, ?> builder) {
    super(builder, true);
  }

  @Override
  public int compareTo(ConfigurationSupplier<C> o) {
    if (o instanceof DefaultConfiguration) {
      return 1;
    }
    if (o instanceof AnnotationConfiguration) {
      return 1;
    }
    return super.compareTo(o);
  }
}
