package io.dekorate.halkyon.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.dekorate.halkyon.config.ComponentConfig;
import io.dekorate.halkyon.config.EditableComponentConfig;

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

}
