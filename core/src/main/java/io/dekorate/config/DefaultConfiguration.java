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

package io.dekorate.config;

import io.fabric8.kubernetes.api.builder.VisitableBuilder;

public class DefaultConfiguration<C> extends ConfigurationSupplier<C> {

  public DefaultConfiguration() {
    super(null);
  }

  public DefaultConfiguration(VisitableBuilder<C, ?> builder) {
    super(builder);
  }

  @Override
  public int compareTo(ConfigurationSupplier<C> o) {
    if (o instanceof AnnotationConfiguration) {
      return -1;
    }
    if (o instanceof PropertyConfiguration) {
      return -1;
    }
    return super.compareTo(o);
  }
}
