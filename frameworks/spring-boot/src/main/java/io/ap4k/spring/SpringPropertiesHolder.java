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
package io.ap4k.spring;

import io.ap4k.WithProject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public interface SpringPropertiesHolder extends WithProject {

    AtomicReference<Map<String, Object>> springProperties = new AtomicReference<>(null);

    default Map<String, Object> getSpringProperties() {
        if (springProperties.get() == null) {
            final Map<String, Object> properties = new HashMap<>();
            properties.putAll(getProject().parseResourceFile("application.properties"));
            properties.putAll(getProject().parseResourceFile("application.yaml"));
            properties.putAll(getProject().parseResourceFile("application.yml"));
            springProperties.set(properties);
        }
        return springProperties.get();
    }
}
