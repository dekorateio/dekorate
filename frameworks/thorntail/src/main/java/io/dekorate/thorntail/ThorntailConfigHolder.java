/**
 * Copyright 2019 The original authors.
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
package io.dekorate.thorntail;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.dekorate.WithProject;

public interface ThorntailConfigHolder extends WithProject {

  AtomicReference<Map<String, Object>> thorntailConfig = new AtomicReference<>(null);

  default Map<String, Object> getThorntailConfig() {
    if (thorntailConfig.get() == null) {
      final Map<String, Object> properties = new HashMap<>();
      properties.putAll(getProject().parseResourceFile("project-defaults.yml"));
      thorntailConfig.set(properties);
    }
    return thorntailConfig.get();
  }
}
