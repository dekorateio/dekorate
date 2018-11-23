package io.ap4k.examples;

import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.utils.Serialization;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class ComponentSpringBootExampleTest {

  @Test
  public void shouldContainComponent() {
    KubernetesList list = Serialization.unmarshal(ComponentSpringBootExampleTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/component.yml"));
    assertNotNull(list);
    assertEquals(1, list.getItems().size());
  }
}
