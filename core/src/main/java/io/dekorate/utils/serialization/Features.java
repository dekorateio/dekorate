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

package io.dekorate.utils.serialization;

import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

public class Features {

  public static boolean exists(String name) {
    return Arrays.stream(Feature.values()).map(Feature::name).anyMatch(n -> n.equals(name));
  }

  public static Optional<Feature> find(String name) {
    return Arrays.stream(Feature.values()).filter(f -> f.name().equals(name)).findFirst();
  }
}
