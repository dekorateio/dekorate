package io.ap4k.component;

import io.ap4k.config.KubernetesConfig;
import io.ap4k.servicecatalog.config.EditableServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentServiceCatalogProcessorTest {

  @Test
  public void shouldAccpetServiceCatalogConfig() {
    ComponentServiceCatalogProcessor generator = new ComponentServiceCatalogProcessor();
    assertTrue(generator.accepts(ServiceCatalogConfig.class));
  }

  @Test
  public void shouldAccpetEditableServiceCatalogConfig() {
    ComponentServiceCatalogProcessor generator = new ComponentServiceCatalogProcessor();
    assertTrue(generator.accepts(EditableServiceCatalogConfig.class));
  }

  @Test
  public void shouldNotAccpetKubernetesConfig() {
    ComponentServiceCatalogProcessor generator = new ComponentServiceCatalogProcessor();
    assertFalse(generator.accepts(KubernetesConfig.class));
  }
}
