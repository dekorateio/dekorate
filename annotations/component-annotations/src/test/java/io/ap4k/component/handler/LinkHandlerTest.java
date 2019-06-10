package io.ap4k.component.handler;

import io.ap4k.component.config.EditableLinkConfig;
import io.ap4k.component.config.LinkConfig;
import io.ap4k.kubernetes.config.BaseConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LinkHandlerTest {

  @Test
  public void shouldAcceptServiceCatalogConfig() {
    LinkHandler generator = new LinkHandler();
    assertTrue(generator.canHandle(LinkConfig.class));
  }

  @Test
  public void shouldAcceptEditableLinkConfig() {
    LinkHandler generator = new LinkHandler();
    assertTrue(generator.canHandle(EditableLinkConfig.class));
  }

  @Test
  public void shouldNotAcceptBaseConfig() {
    LinkHandler generator = new LinkHandler();
    assertFalse(generator.canHandle(BaseConfig.class));
  }
}
