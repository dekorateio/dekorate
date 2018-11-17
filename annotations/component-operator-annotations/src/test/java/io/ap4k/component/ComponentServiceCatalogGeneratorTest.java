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
    ServiceCatalogGenerator generator = new ServiceCatalogGenerator();
    assertTrue(generator.accepts(ServiceCatalogConfig.class));
  }

  @Test
  public void shouldAccpetEditableServiceCatalogConfig() {
    ServiceCatalogGenerator generator = new ServiceCatalogGenerator();
    assertTrue(generator.accepts(EditableServiceCatalogConfig.class));
  }

  @Test
  public void shouldNotAccpetKubernetesConfig() {
    ServiceCatalogGenerator generator = new ServiceCatalogGenerator();
    assertFalse(generator.accepts(KubernetesConfig.class));
  }
}
