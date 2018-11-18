package io.ap4k.examples.s2i;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStream;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SourceToImageExampleTest {

  @Test
  public void shouldContainDeploymentConfig() {
    KubernetesList list = Serialization.unmarshal(SourceToImageExampleTest.class.getClassLoader().getResourceAsStream("/META-INF/apk4/openshift.yml"));
    assertNotNull(list);
    DeploymentConfig dc = findFirst(list, DeploymentConfig.class).orElseThrow(()-> new IllegalStateException());
    assertNotNull(dc);
  }

  @Test
  public void shouldContainBuildConfig() {
    KubernetesList list = Serialization.unmarshal(SourceToImageExampleTest.class.getClassLoader().getResourceAsStream("/META-INF/apk4/openshift.yml"));
    assertNotNull(list);
    BuildConfig bc = findFirst(list, BuildConfig.class).orElseThrow(()-> new IllegalStateException());
    assertNotNull(bc);
  }

  @Test
  public void shouldContainImageStream() {
    KubernetesList list = Serialization.unmarshal(SourceToImageExampleTest.class.getClassLoader().getResourceAsStream("/META-INF/apk4/openshift.yml"));
    assertNotNull(list);
    ImageStream is = findFirst(list, ImageStream.class).orElseThrow(()-> new IllegalStateException());
    assertNotNull(is);
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }

}
