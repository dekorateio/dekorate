package io.ap4k.servicecatalog;

import io.ap4k.config.KubernetesConfig;
import io.ap4k.servicecatalog.config.EditableServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceCatalogProcessorTest {

  @Test
  public void shouldAccpetServiceCatalogConfig() {
    ServiceCatalogProcessor generator = new ServiceCatalogProcessor();
    assertTrue(generator.accepts(ServiceCatalogConfig.class));
  }

  @Test
  public void shouldAccpetEditableServiceCatalogConfig() {
    ServiceCatalogProcessor generator = new ServiceCatalogProcessor();
    assertTrue(generator.accepts(EditableServiceCatalogConfig.class));
  }

  @Test
  public void shouldNotAccpetKubernetesConfig() {
    ServiceCatalogProcessor generator = new ServiceCatalogProcessor();
    assertFalse(generator.accepts(KubernetesConfig.class));
  }
}
