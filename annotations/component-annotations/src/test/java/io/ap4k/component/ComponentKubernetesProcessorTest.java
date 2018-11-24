package io.ap4k.component;

import io.ap4k.config.EditableKubernetesConfig;
import io.ap4k.config.KubernetesConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentKubernetesProcessorTest {

  @Test
  public void shouldAccpetKubernetesConfig()  {
    ComponentKubernetesProcessor generator = new ComponentKubernetesProcessor();
    assertTrue(generator.accepts(KubernetesConfig.class));
  }

  @Test
  public void shouldAccpetEditableKubernetesConfig()  {
    ComponentKubernetesProcessor generator = new ComponentKubernetesProcessor();
    assertTrue(generator.accepts(EditableKubernetesConfig.class));
  }
}
