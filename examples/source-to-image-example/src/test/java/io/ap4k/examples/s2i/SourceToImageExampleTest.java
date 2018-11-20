package io.ap4k.examples.s2i;

import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.openshift.api.model.BuildConfig;
import io.ap4k.deps.openshift.api.model.DeploymentConfig;
import io.ap4k.deps.openshift.api.model.ImageStream;
import io.ap4k.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SourceToImageExampleTest {

  @Test
  public void shouldContainDeploymentConfig() {
    KubernetesList list = Serialization.unmarshal(SourceToImageExampleTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/openshift.yml"));
    assertNotNull(list);
    DeploymentConfig dc = findFirst(list, DeploymentConfig.class).orElseThrow(()-> new IllegalStateException());
    assertNotNull(dc);
  }

  @Test
  public void shouldContainBuildConfig() {
    KubernetesList list = Serialization.unmarshal(SourceToImageExampleTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/openshift.yml"));
    assertNotNull(list);
    BuildConfig bc = findFirst(list, BuildConfig.class).orElseThrow(()-> new IllegalStateException());
    assertNotNull(bc);
  }

  @Test
  public void shouldContainImageStream() {
    KubernetesList list = Serialization.unmarshal(SourceToImageExampleTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/openshift.yml"));
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
