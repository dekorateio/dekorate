package io.ap4k.openshift.generator;

import io.ap4k.Session;
import io.ap4k.SessionWriter;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.apps.Deployment;
import io.ap4k.openshift.annotation.OpenshiftApplication;
import io.ap4k.processor.SimpleFileWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenshiftApplicationGeneratorTest {
  static Path tempDir;

  @BeforeAll
  public static void setup() throws IOException {
    tempDir = Files.createTempDirectory("ap4k");
  }

  @Test
  public void shouldGenerateKubernetes()  {
    SessionWriter writer = new SimpleFileWriter(tempDir);
    Session session = Session.getSession();
    session.setWriter(writer);

    OpenshiftApplicationGenerator generator = new OpenshiftApplicationGenerator() {};

    Map<String, Object> map = new HashMap<String, Object>() {{
      put(OpenshiftApplication.class.getName(), new HashMap<String, Object>() {{
        put("name", "generator-test");
        put("version", "latest");
        put("replicas", 2);
      }});
    }};

    generator.add(map);
    session.close();
    KubernetesList list=session.getGeneratedResources().get("kubernetes");
    assertThat(list).isNotNull();
    assertThat(list.getItems())
      .filteredOn(i -> "DeploymentConfig".equals(i.getKind()))
      .filteredOn(i -> ((DeploymentConfig)i).getSpec().getReplicas() == 2)
      .isNotEmpty();
  }
}
