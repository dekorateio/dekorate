package io.ap4k.example.sbonkubernetes;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SpringBootOnKubernetesTest {

  @Test
  public void shouldContainDeployment() {
    KubernetesList list = Serialization.unmarshal(SpringBootOnKubernetesTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/kubernetes.yml"));
    assertNotNull(list);
    Deployment d = findFirst(list, Deployment.class).orElseThrow(()-> new IllegalStateException());
    assertNotNull(d);
  }

  @Test
  public void shouldContainService() {
    KubernetesList list = Serialization.unmarshal(SpringBootOnKubernetesTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/kubernetes.yml"));
    assertNotNull(list);
    Service s = findFirst(list, Service.class).orElseThrow(()-> new IllegalStateException());
    assertNotNull(s);
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
