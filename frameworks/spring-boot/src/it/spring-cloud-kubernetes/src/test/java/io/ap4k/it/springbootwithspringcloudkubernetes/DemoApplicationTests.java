package io.ap4k.it.springbootwithspringcloudkubernetes;

import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.ap4k.utils.Serialization;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.rbac.KubernetesRoleBinding;
import io.ap4k.deps.kubernetes.api.model.ServiceAccount;
import java.util.Optional;

public class DemoApplicationTests {

  @Test
  public void shouldContainerServiceAccount() {
    KubernetesList list = Serialization.unmarshal(getClass().getClassLoader().getResourceAsStream("META-INF/ap4k/kubernetes.yml"));
    Assert.assertNotNull(list);
    ServiceAccount serviceAccount = findFirst(list, ServiceAccount.class).orElseThrow(IllegalStateException::new);
    //Desrializing rbac resources is broken at the moment: https://github.com/fabric8io/kubernetes-client/issues/1531
    //KubernetesRoleBinding roleBinding = findFirst(list, KubernetesRoleBinding.class).orElseThrow(IllegalStateException::new);
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
