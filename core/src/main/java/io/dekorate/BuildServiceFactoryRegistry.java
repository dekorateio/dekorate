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

package io.dekorate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

public class BuildServiceFactoryRegistry {

  private static final Set<BuildServiceFactory> registry = new HashSet<>();
  private static final Set<String> blacklist = new HashSet<>();

  static {
    ServiceLoader.load(BuildServiceFactory.class, BuildServiceFactoryRegistry.class.getClassLoader()).forEach(b -> registry.add(b));
  }

  public static Set<BuildServiceFactory> getBuildServiceFacotries() {
       return registry.stream().filter(r -> !blacklist.contains(r.getClass().getSimpleName())).collect(Collectors.toSet());
  }

  public static void register(BuildServiceFactory item) {
    registry.add(item);
  }

  public static void blacklist(String... className) {
    Arrays.asList(className).forEach(c -> blacklist.add(c));
  }
}
