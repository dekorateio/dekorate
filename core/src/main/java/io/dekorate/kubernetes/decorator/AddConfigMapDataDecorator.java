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

package io.dekorate.kubernetes.decorator;

import java.util.Map;

import io.dekorate.utils.Maps;
import io.fabric8.kubernetes.api.model.ConfigMapFluent;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class AddConfigMapDataDecorator extends NamedResourceDecorator<ConfigMapFluent<?>> {

  private final Map<String, String> map;

  public AddConfigMapDataDecorator(String name, String... keyValues) {
    this(name, Maps.from(keyValues));
  }

  public AddConfigMapDataDecorator(String name, Map<String, String> map) {
    super(name);
    this.map = map;
  }

  @Override
  public void andThenVisit(ConfigMapFluent<?> config, ObjectMeta resourceMeta) {
    config.addToData(map);
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, AddConfigMapDataDecorator.class };
  }
}
