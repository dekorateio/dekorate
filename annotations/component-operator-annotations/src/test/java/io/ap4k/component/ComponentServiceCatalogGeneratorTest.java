package io.ap4k.component;

import io.ap4k.config.KubernetesConfig;
import io.ap4k.servicecatalog.ServiceCatalogGenerator;
import io.ap4k.servicecatalog.config.EditableServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentServiceCatalogGeneratorTest {

  @Test
  public void shouldAccpetServiceCatalogConfig() {
    ComponentServiceCatalogGenerator generator = new ComponentServiceCatalogGenerator();
    assertTrue(generator.accepts(ServiceCatalogConfig.class));
  }

  @Test
  public void shouldAccpetEditableServiceCatalogConfig() {
    ComponentServiceCatalogGenerator generator = new ComponentServiceCatalogGenerator();
    assertTrue(generator.accepts(EditableServiceCatalogConfig.class));
  }

  @Test
  public void shouldNotAccpetKubernetesConfig() {
    ComponentServiceCatalogGenerator generator = new ComponentServiceCatalogGenerator();
    assertFalse(generator.accepts(KubernetesConfig.class));
  }
}
