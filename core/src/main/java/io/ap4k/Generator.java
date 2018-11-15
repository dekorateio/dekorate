package io.ap4k;

import io.ap4k.config.Configuration;

public interface Generator<C extends Configuration> {

  /**
   * Generate / populate the resources.
   * @param config
   */
    void generate(C config);


     Class<? extends C> getType();
}
