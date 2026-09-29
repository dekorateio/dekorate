package io.dekorate.core;

import java.util.List;

import io.fabric8.kubernetes.api.model.HasMetadata;

public interface SessionListener {

  default void onGeneratorStarted(Generator generator) {
  }

  default void onResourceGenerated(Generator generator, HasMetadata resource) {
  }

  default void onAllGenerated(List<HasMetadata> resources) {
  }
}
