package io.dekorate.core;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.jboss.logging.Logger;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.utils.Serialization;

public class Session implements Closeable {

  private static final Logger LOG = Logger.getLogger(Session.class);

  public enum State {
    CREATED, LOADED, STARTED, CLOSED
  }

  private State state = State.CREATED;
  private final ConfigurationGenerator configGenerator;
  private Configuration config;
  private VisitorRegistry visitorRegistry;
  private final List<Generator> generators = new ArrayList<>();
  private final List<SessionListener> listeners = new ArrayList<>();
  private List<HasMetadata> resources;
  private Path exportPath = Paths.get("target");

  public Session(ConfigurationGenerator configGenerator) {
    this.configGenerator = configGenerator;
  }

  public Session withExportPath(Path exportPath) {
    this.exportPath = exportPath;
    return this;
  }

  public Session addListener(SessionListener listener) {
    listeners.add(listener);
    return this;
  }

  public Session load() {
    if (state != State.CREATED) {
      throw new IllegalStateException("Session already loaded (state: " + state + ")");
    }

    config = configGenerator.generate();
    LOG.info("Configuration loaded");

    visitorRegistry = new VisitorRegistry(config);

    int discovered = 0;
    for (Generator generator : ServiceLoader.load(Generator.class)) {
      LOG.debugf("ServiceLoader discovered generator: %s", generator.getClass().getName());
      generators.add(generator);
      discovered++;
    }
    LOG.infof("ServiceLoader discovered %d generators", discovered);

    state = State.LOADED;
    return this;
  }

  public Session register(Generator generator) {
    if (state == State.CLOSED) {
      throw new IllegalStateException("Session is closed");
    }
    if (state == State.STARTED) {
      throw new IllegalStateException("Session already started");
    }
    for (Generator existing : generators) {
      if (existing.getClass().equals(generator.getClass())) {
        LOG.debugf("Generator already registered, skipping: %s", generator.getClass().getName());
        return this;
      }
    }
    LOG.debugf("Manually registered generator: %s", generator.getClass().getName());
    generators.add(generator);
    return this;
  }

  public List<HasMetadata> start() {
    if (state != State.LOADED) {
      throw new IllegalStateException("Session must be loaded before starting (state: " + state + ")");
    }

    resources = new ArrayList<>();
    for (Generator generator : generators) {
      LOG.infof("Running generator %s (group: %s)",
          generator.getClass().getSimpleName(), generator.getGroup());
      for (SessionListener listener : listeners) {
        listener.onGeneratorStarted(generator);
      }
      HasMetadata resource = generator.generate(config, visitorRegistry);
      resources.add(resource);
      for (SessionListener listener : listeners) {
        listener.onResourceGenerated(generator, resource);
      }
    }
    LOG.infof("Generated %d resources", resources.size());
    for (SessionListener listener : listeners) {
      listener.onAllGenerated(Collections.unmodifiableList(resources));
    }

    state = State.STARTED;
    return Collections.unmodifiableList(resources);
  }

  public List<HasMetadata> getResources() {
    if (state != State.STARTED) {
      throw new IllegalStateException("Session must be started before accessing resources (state: " + state + ")");
    }
    return Collections.unmodifiableList(resources);
  }

  @Override
  public void close() {
    if (resources != null && !resources.isEmpty()) {
      exportResources();
    }
    generators.clear();
    resources = null;
    config = null;
    visitorRegistry = null;
    state = State.CLOSED;
    LOG.info("Session closed");
  }

  private void exportResources() {
    try {
      Files.createDirectories(exportPath);
      Path manifestPath = exportPath.resolve("kubernetes.yml");
      StringBuilder yaml = new StringBuilder();
      for (HasMetadata resource : resources) {
        String resourceYaml = Serialization.asYaml(resource);
        if (yaml.length() > 0 && !resourceYaml.startsWith("---")) {
          yaml.append("---\n");
        }
        yaml.append(resourceYaml);
      }
      Files.write(manifestPath, yaml.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
      LOG.infof("Exported %d resources to %s", resources.size(), manifestPath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to export resources to: " + exportPath, e);
    }
  }

  public State getState() {
    return state;
  }

  public Configuration getConfig() {
    return config;
  }

  public VisitorRegistry getVisitorRegistry() {
    return visitorRegistry;
  }

  public List<Generator> getGenerators() {
    return generators;
  }
}
