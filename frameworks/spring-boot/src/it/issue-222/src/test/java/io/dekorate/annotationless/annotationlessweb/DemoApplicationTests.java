package io.dekorate.annotationless.annotationlessweb;

import io.dekorate.utils.Serialization;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.Service;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DemoApplicationTests {

  @Test
  public void shouldContainService() {
    KubernetesList list = Serialization.unmarshal(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Service service = findFirst(list, Service.class).orElseThrow(IllegalStateException::new);
    assertNotNull(service);
    assertEquals(1, list.getItems().stream().filter(i -> Service.class.isInstance(i)).count());
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
