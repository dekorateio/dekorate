package io.dekorate.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.jboss.logging.Logger;

import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.builder.VisitableBuilder;

public class VisitorRegistry {

  private static final Logger LOG = Logger.getLogger(VisitorRegistry.class);

  private final Configuration config;
  private final List<VisitorFactory> factories = new ArrayList<>();

  public VisitorRegistry(Configuration config) {
    this.config = config;
    for (VisitorFactory factory : ServiceLoader.load(VisitorFactory.class, VisitorFactory.class.getClassLoader())) {
      LOG.debugf("ServiceLoader discovered: %s", factory.getClass().getName());
      factories.add(factory);
    }
    LOG.infof("ServiceLoader discovered %d visitor factories", factories.size());
  }

  public VisitorRegistry register(VisitorFactory factory) {
    LOG.debugf("Manually registered: %s", factory.getClass().getName());
    factories.add(factory);
    return this;
  }

  public Configuration getConfig() {
    return config;
  }

  public List<VisitorFactory> getFactories() {
    return factories;
  }

  public void applyAll(VisitableBuilder<?, ?> builder, String... groups) {
    Set<String> groupSet = new HashSet<>(Arrays.asList(groups));
    int applied = 0;
    for (VisitorFactory factory : factories) {
      if (!groupSet.contains(factory.getGroup())) {
        continue;
      }
      String keyPath = factory.getKeyPath();
      Object value = config.resolve(keyPath);
      if (value != null) {
        LOG.infof("Applying visitor %s for key path: %s (group: %s)",
            factory.getClass().getSimpleName(), keyPath, factory.getGroup());
        TypedVisitor<?> visitor = factory.create(config);
        builder.accept(visitor);
        applied++;
      } else {
        LOG.debugf("Skipping visitor %s — key path not found: %s",
            factory.getClass().getSimpleName(), keyPath);
      }
    }
    LOG.infof("Visitors applied: %d (groups: %s)", applied, String.join(", ", groups));
  }

  public void applyAll(VisitableBuilder<?, ?> builder) {
    int applied = 0;
    for (VisitorFactory factory : factories) {
      String keyPath = factory.getKeyPath();
      Object value = config.resolve(keyPath);

      if (value != null) {
        LOG.infof("Applying visitor %s for key path: %s", factory.getClass().getSimpleName(), keyPath);
        TypedVisitor<?> visitor = factory.create(config);
        builder.accept(visitor);
        applied++;
      } else {
        LOG.debugf("Skipping visitor %s — key path not found: %s", factory.getClass().getSimpleName(), keyPath);
      }
    }
    LOG.infof("Visitors applied: %d out of %d registered", applied, factories.size());

    Set<String> handledPaths = new HashSet<>();
    for (VisitorFactory factory : factories) {
      handledPaths.add(factory.getKeyPath());
    }

    for (String configKey : config.collectKeyPaths()) {
      boolean handled = false;
      for (String handledPath : handledPaths) {
        if (configKey.equals(handledPath)
            || configKey.startsWith(handledPath + ".")
            || handledPath.startsWith(configKey + ".")) {
          handled = true;
          break;
        }
      }
      if (!handled) {
        LOG.warnf("No visitor registered for configuration key: %s", configKey);
      }
    }
  }
}
