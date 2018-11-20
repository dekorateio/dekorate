package io.ap4k.component;

import io.ap4k.Generator;
import io.ap4k.Resources;
import io.ap4k.component.config.CompositeConfig;
import io.ap4k.component.config.EditableCompositeConfig;
import io.ap4k.config.Configuration;

public class ComponentGenerator implements Generator<CompositeConfig> {

  private static final String COMPONENT = "component";

  private final Resources resources;

  public ComponentGenerator () {
    this(new Resources());
  }
  public ComponentGenerator(Resources resources) {
    this.resources = resources;
  }


  @Override
  public void generate(CompositeConfig config) {

  }

  @Override
  public boolean accepts(Class<? extends Configuration> type) {
   return type.equals(CompositeConfig.class) ||
   type.equals(EditableCompositeConfig.class);
  }
}
