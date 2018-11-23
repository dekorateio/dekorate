package io.ap4k.examples.component;

import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.utils.Serialization;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class ComponentServiceSpringBootExampleTest {

  @Test
  public void shouldContainDeploymentConfig() {
    KubernetesList list = Serialization.unmarshal(ComponentServiceSpringBootExampleTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/component.yml"));
    assertNotNull(list);
    assertEquals(1, list.getItems().size());
  }
}
