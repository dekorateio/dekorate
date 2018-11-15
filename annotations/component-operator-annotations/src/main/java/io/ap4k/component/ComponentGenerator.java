package io.ap4k.component;

import io.ap4k.Generator;
import io.ap4k.Resources;
import io.ap4k.component.config.CompositeConfig;

public class ComponentGenerator implements Generator<CompositeConfig> {

  private static final String COMPONENT = "component";

  private final Resources resources;

  public ComponentGenerator(Resources resources) {
    this.resources = resources;
  }


  @Override
  public void generate(CompositeConfig config) {

  }

  @Override
  public Class<? extends CompositeConfig> getType() {
    return CompositeConfig.class;
  }
}
