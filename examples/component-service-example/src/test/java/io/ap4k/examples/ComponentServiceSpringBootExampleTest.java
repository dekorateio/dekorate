package io.ap4k.examples;

import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.servicecatalog.api.model.ServiceInstance;
import io.ap4k.utils.Serialization;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class ComponentServiceSpringBootExampleTest {

  @Test
  public void shouldContainComponentService() {
    KubernetesList list = Serialization.unmarshal(ComponentServiceSpringBootExampleTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/component.yml"));
    assertNotNull(list);
    List<HasMetadata> items = list.getItems();
    assertEquals(1, items.size());
    ServiceInstance svc = (ServiceInstance)items.get(0);
    assertNotNull(svc);
    assertEquals("mysql-instance",svc.getMetadata().getName());
  }
}
