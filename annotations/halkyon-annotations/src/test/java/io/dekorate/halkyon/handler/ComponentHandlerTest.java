package io.dekorate.halkyon.handler;

import io.dekorate.halkyon.config.EditableHalkyonConfig;
import io.dekorate.halkyon.config.HalkyonConfig;
import io.dekorate.kubernetes.config.BaseConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComponentHandlerTest {
  
  @Test
  public void shouldAcceptServiceCatalogConfig() {
    ComponentHandler generator = new ComponentHandler();
    assertTrue(generator.canHandle(HalkyonConfig.class));
  }
  
  @Test
  public void shouldAcceptEditableLinkConfig() {
    ComponentHandler generator = new ComponentHandler();
    assertTrue(generator.canHandle(EditableHalkyonConfig.class));
  }
  
  @Test
  public void shouldNotAcceptBaseConfig() {
    LinkHandler generator = new LinkHandler();
    assertFalse(generator.canHandle(BaseConfig.class));
  }
}
