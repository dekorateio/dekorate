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
