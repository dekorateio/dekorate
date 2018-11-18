package io.ap4k.example.sbonopenshift;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.fabric8.openshift.api.model.DeploymentConfig;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SpringBootOnOpenshiftTest {
  @Test
  public void shouldContainDeployment() {
    KubernetesList list = Serialization.unmarshal(SpringBootOnOpenshiftTest.class.getClassLoader().getResourceAsStream("/META-INF/apk4/openshift.yml"));
    assertNotNull(list);
    DeploymentConfig dc = findFirst(list, DeploymentConfig.class).orElseThrow(()-> new IllegalStateException());
    assertNotNull(dc);
  }

  @Test
  public void shouldContainService() {
    KubernetesList list = Serialization.unmarshal(SpringBootOnOpenshiftTest.class.getClassLoader().getResourceAsStream("/META-INF/apk4/openshift.yml"));
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
