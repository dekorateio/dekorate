package io.ap4k;

import io.ap4k.config.Configuration;

public interface Generator<C extends Configuration> {

  /**
   * Generate / populate the resources.
   * @param config
   */
  void generate(C config);


  /**
   * Check if config is accepted.
   * A generator can choose to which configuration it should react.
   * @param config The specified config class;
   * @returns True if config type is accepted, false otherwise.
   */
  boolean accepts(Class<? extends Configuration> config);
}
