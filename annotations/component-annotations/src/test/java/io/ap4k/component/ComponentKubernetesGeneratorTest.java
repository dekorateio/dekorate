package io.ap4k.component;

import io.ap4k.config.EditableKubernetesConfig;
import io.ap4k.config.KubernetesConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentKubernetesGeneratorTest {

  @Test
  public void shouldAccpetKubernetesConfig()  {
    ComponentKubernetesGenerator generator = new ComponentKubernetesGenerator();
    assertTrue(generator.accepts(KubernetesConfig.class));
  }

  @Test
  public void shouldAccpetEditableKubernetesConfig()  {
    ComponentKubernetesGenerator generator = new ComponentKubernetesGenerator();
    assertTrue(generator.accepts(EditableKubernetesConfig.class));
  }
}
