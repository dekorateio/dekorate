package io.dekorate.servicebinding.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.dekorate.Session;
import io.dekorate.SessionWriter;
import io.dekorate.WithProject;
import io.dekorate.processor.SimpleFileWriter;
import io.dekorate.project.FileProjectFactory;
import io.dekorate.servicebinding.model.ServiceBinding;
import io.fabric8.kubernetes.api.model.KubernetesList;

public class ServiceBindingConfigGeneratorTest {
  @Test
  public void shouldGenerateServiceBindingWithoutWritingToFileSystem() throws IOException {
    Path tempDir = Files.createTempDirectory("dekorate");

    WithProject withProject = new WithProject() {
    };
    withProject
        .setProject(FileProjectFactory.create(new File(".")).withDekorateOutputDir(tempDir.toAbsolutePath().toString())
            .withDekorateMetaDir(tempDir.toAbsolutePath().toString()));

    SessionWriter writer = new SimpleFileWriter(withProject.getProject(), false);
    Session session = Session.getSession();
    session.setWriter(writer);

    ServiceBindingConfigGenerator generator = new DefaultServiceBindingConfigGenerator(session.getConfigurationRegistry());

    final HashMap<String, Object> services[] = new HashMap[1];
    services[0] = new HashMap<String, Object>() {
      {
        put("group", "postgresql.dev");
        put("kind", "Database");
        put("name", "demo-database");
        put("id", "postgresDB");
      }
    };

    final HashMap<String, Object> customEnvVar[] = new HashMap[1];
    customEnvVar[0] = new HashMap<String, Object>() {
      {
        put("name", "foo");
        put("value", "bar");
        put("configmap", "baz");
      }
    };

    final HashMap<String, Object> bindingPath = new HashMap<String, Object>();
    bindingPath.put("secretPath", "/var");
    bindingPath.put("containerPath", ".spec");

    final HashMap<String, Object> application = new HashMap<String, Object>();
    application.put("group", "apps");
    application.put("kind", "Deployment");
    application.put("version", "v1");
    application.put("name", "servicebinding-test");

    Map<String, Object> map = new HashMap<String, Object>() {
      {
        put(ServiceBindingConfig.class.getName(), new HashMap<String, Object>() {
          {
            put("name", "servicebinding-test-binding");
            put("application", application);
            put("services", services);
            put("envVarPrefix", "postgres");
            put("bindingPath", bindingPath);
            put("customEnvVar", customEnvVar);
          }
        });
      }
    };

    generator.addPropertyConfiguration(map);
    final Map<String, String> result = session.close();
    KubernetesList list = session.getGeneratedResources().get("kubernetes");
    assertThat(list).isNotNull();
    assertThat(list.getItems()).filteredOn(i -> "ServiceBinding".equals(i.getKind()))
        .hasOnlyOneElementSatisfying(item -> {
          assertThat(item).isInstanceOfSatisfying(ServiceBinding.class, s -> {
            assertThat(s.getSpec()).satisfies(spec -> {
              assertThat(spec.getEnvVarPrefix()).isEqualTo("postgres");
              assertThat(spec.getApplication().getKind()).isEqualTo("Deployment");
              assertThat(spec.getApplication().getResource()).isEqualTo("deployments");
              assertThat(spec.getApplication().getVersion()).isEqualTo("v1");
              assertThat(spec.getApplication().getName()).isEqualTo("servicebinding-test");
              assertThat(spec.getApplication().getGroup()).isEqualTo("apps");
              assertThat(spec.getApplication().getBindingPath().getContainerPath()).isEqualTo(".spec");
              assertThat(spec.getApplication().getBindingPath().getSecretPath()).isEqualTo("/var");
              assertThat(spec.getServices()[0].getGroup()).isEqualTo("postgresql.dev");
              assertThat(spec.getServices()[0].getName()).isEqualTo("demo-database");
              assertThat(spec.getServices()[0].getId()).isEqualTo("postgresDB");
              assertThat(spec.getServices()[0].getKind()).isEqualTo("Database");
              assertThat(spec.getCustomEnvVar()[0].getName()).isEqualTo("foo");
              assertThat(spec.getCustomEnvVar()[0].getValueFrom().getConfigMapKeyRef().getKey()).isEqualTo("bar");
              assertThat(spec.getCustomEnvVar()[0].getValueFrom().getConfigMapKeyRef().getName()).isEqualTo("baz");
            });
          });
        });

    assertThat(tempDir.resolve("kubernetes.json")).doesNotExist();
    assertThat(tempDir.resolve("kubernetes.yml")).doesNotExist();

    assertThat(result).hasSize(7);
  }
}
