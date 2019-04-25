package io.ap4k.kubernetes.generator;

import io.ap4k.Session;
import io.ap4k.SessionWriter;
import io.ap4k.WithProject;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.apps.Deployment;
import io.ap4k.kubernetes.annotation.KubernetesApplication;
import io.ap4k.processor.SimpleFileWriter;
import io.ap4k.project.FileProjectFactory;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KubernetesApplicationGeneratorTest {

  @Test
  public void shouldGenerateKubernetesWithoutWritingToFileSystem() throws IOException {
    Path tempDir = Files.createTempDirectory("ap4k");

    SessionWriter writer = new SimpleFileWriter(tempDir, false);
    Session session = Session.getSession();
    session.setWriter(writer);

    KubernetesApplicationGenerator generator = new KubernetesApplicationGenerator() {};
    generator.setProject(FileProjectFactory.create(new File(".")));

    Map<String, Object> map = new HashMap<String, Object>() {{
      put(KubernetesApplication.class.getName(), new HashMap<String, Object>() {{
        put("name", "generator-test");
        put("group", "generator-test-group");
        put("version", "latest");
        put("replicas", 2);
      }});
    }};

    generator.add(map);
    final Map<String, String> result = session.close();
    KubernetesList list=session.getGeneratedResources().get("kubernetes");
    assertThat(list).isNotNull();
    assertThat(list.getItems())
            .filteredOn(i -> "Deployment".equals(i.getKind()))
            .filteredOn(i -> ((Deployment)i).getSpec().getReplicas() == 2)
            .isNotEmpty();

    assertThat(tempDir.resolve("kubernetes.json")).doesNotExist();
    assertThat(tempDir.resolve("kubernetes.yml")).doesNotExist();

    assertThat(result).hasSize(4);
  }
}
