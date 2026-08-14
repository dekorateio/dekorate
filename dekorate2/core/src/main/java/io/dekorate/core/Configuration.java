package io.dekorate.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Configuration {

  private final Map<String, Object> properties;

  public Configuration() {
    this(new LinkedHashMap<>());
  }

  public Configuration(Map<String, Object> properties) {
    this.properties = properties != null ? new LinkedHashMap<>(properties) : new LinkedHashMap<>();
  }

  @SuppressWarnings("unchecked")
  public static Configuration fromProperties(Path path) throws IOException {
    Properties props = new Properties();
    try (InputStream in = Files.newInputStream(path)) {
      props.load(in);
    }
    Map<String, Object> root = new LinkedHashMap<>();
    for (String key : props.stringPropertyNames()) {
      String[] segments = key.split("\\.");
      Map<String, Object> current = root;
      for (int i = 0; i < segments.length - 1; i++) {
        current = (Map<String, Object>) current.computeIfAbsent(segments[i], k -> new LinkedHashMap<>());
      }
      current.put(segments[segments.length - 1], props.getProperty(key));
    }
    return new Configuration(root);
  }

  public Configuration put(String key, Object value) {
    properties.put(key, value);
    return this;
  }

  public Object get(String key) {
    return properties.get(key);
  }

  public String getString(String key) {
    Object v = properties.get(key);
    return v != null ? v.toString() : null;
  }

  public Integer getInteger(String key) {
    Object v = properties.get(key);
    if (v instanceof Integer) {
      return (Integer) v;
    }
    if (v instanceof Number) {
      return ((Number) v).intValue();
    }
    if (v instanceof String) {
      return Integer.parseInt((String) v);
    }
    return null;
  }

  public Boolean getBoolean(String key) {
    Object v = properties.get(key);
    if (v instanceof Boolean) {
      return (Boolean) v;
    }
    if (v instanceof String) {
      return Boolean.parseBoolean((String) v);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getMap(String key) {
    Object v = properties.get(key);
    if (v instanceof Map) {
      return (Map<String, Object>) v;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public Configuration getSubConfig(String key) {
    Object v = properties.get(key);
    if (v instanceof Configuration) {
      return (Configuration) v;
    }
    if (v instanceof Map) {
      return new Configuration((Map<String, Object>) v);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public Object resolve(String path) {
    String[] segments = path.split("\\.");
    Map<String, Object> current = properties;
    for (int i = 0; i < segments.length - 1; i++) {
      Object next = current.get(segments[i]);
      if (next instanceof Map) {
        current = (Map<String, Object>) next;
      } else {
        return null;
      }
    }
    return current.get(segments[segments.length - 1]);
  }

  public String resolveString(String path) {
    Object v = resolve(path);
    return v != null ? v.toString() : null;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> resolveMap(String path) {
    Object v = resolve(path);
    if (v instanceof Map) {
      return (Map<String, Object>) v;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public Set<String> collectKeyPaths() {
    Set<String> paths = new HashSet<>();
    collectKeyPaths(properties, "", paths);
    return paths;
  }

  @SuppressWarnings("unchecked")
  private void collectKeyPaths(Map<String, Object> map, String prefix, Set<String> paths) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String path = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
      if (entry.getValue() instanceof Map) {
        paths.add(path);
        collectKeyPaths((Map<String, Object>) entry.getValue(), path, paths);
      } else {
        paths.add(path);
      }
    }
  }

  public boolean has(String key) {
    return properties.containsKey(key);
  }

  public Map<String, Object> asMap() {
    return Collections.unmodifiableMap(properties);
  }
}
