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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.dekorate.WithProject;
import io.dekorate.utils.Maps;

public interface ThorntailConfigHolder extends WithProject {

  AtomicReference<Map<String, Object>> thorntailConfig = new AtomicReference<>(null);
  final static String PROJECT_DEFAULTS_YML = "project-defaults.yml";

  default Map<String, Object> getThorntailConfig() {
    if (thorntailConfig.get() == null) {
      final Map<String, Object> properties = new HashMap<>();
      try (InputStream is = new FileInputStream(
          getProject().getBuildInfo().getResourceDir().resolve(PROJECT_DEFAULTS_YML).toFile())) {
        properties.putAll(Maps.parseResourceFile(is, PROJECT_DEFAULTS_YML));
        thorntailConfig.set(properties);
      } catch (FileNotFoundException e) {
        return properties;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return thorntailConfig.get();
  }
}
