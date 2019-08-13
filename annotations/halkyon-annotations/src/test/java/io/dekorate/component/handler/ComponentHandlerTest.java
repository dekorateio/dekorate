package io.dekorate.component.handler;

import io.dekorate.component.config.ComponentConfig;
import io.dekorate.component.config.EditableComponentConfig;
import io.dekorate.component.config.EditableLinkConfig;
import io.dekorate.component.config.LinkConfig;
import io.dekorate.kubernetes.config.BaseConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComponentHandlerTest {

  @Test
  public void shouldAcceptServiceCatalogConfig() {
    ComponentHandler generator = new ComponentHandler();
    assertTrue(generator.canHandle(ComponentConfig.class));
  }

  @Test
  public void shouldAcceptEditableLinkConfig() {
    ComponentHandler generator = new ComponentHandler();
    assertTrue(generator.canHandle(EditableComponentConfig.class));
  }

  @Test
  public void shouldNotAcceptBaseConfig() {
    LinkHandler generator = new LinkHandler();
    assertFalse(generator.canHandle(BaseConfig.class));
  }
}
