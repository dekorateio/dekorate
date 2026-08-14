package io.dekorate.core;

import java.io.IOException;
import java.nio.file.Path;

public class PropertiesConfigurationGenerator implements ConfigurationGenerator {

  private final Path path;

  public PropertiesConfigurationGenerator(Path path) {
    this.path = path;
  }

  @Override
  public Configuration generate() {
    try {
      return Configuration.fromProperties(path);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load properties from: " + path, e);
    }
  }
}
