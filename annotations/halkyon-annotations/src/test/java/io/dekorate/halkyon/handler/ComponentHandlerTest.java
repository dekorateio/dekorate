package io.dekorate.halkyon.handler;

import io.dekorate.halkyon.config.ComponentConfig;
import io.dekorate.halkyon.config.EditableComponentConfig;
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
  
}
