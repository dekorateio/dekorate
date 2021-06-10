package io.dekorate.servicebinding.manfiest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dekorate.Session;
import io.dekorate.WithProject;
import io.dekorate.project.FileProjectFactory;
import io.dekorate.servicebinding.config.EditableServiceBindingConfig;
import io.dekorate.servicebinding.config.ServiceBindingConfig;
import io.dekorate.servicebinding.manifest.ServiceBindingResourceGenerator;

public class ServiceBindingResourceGeneratorTest {

  @BeforeAll
  public static void setup() throws IOException {
    Path tempDir = Files.createTempDirectory("dekorate");
    WithProject withProject = new WithProject() {
    };
    withProject.setProject(FileProjectFactory.create(new File(".")).withDekorateOutputDir(tempDir.toAbsolutePath().toString())
        .withDekorateMetaDir(tempDir.toAbsolutePath().toString()));
  }

  @Test
  public void shouldAcceptServiceBindingConfig() {
    Session session = Session.getSession();
    ServiceBindingResourceGenerator handler = new ServiceBindingResourceGenerator(session.getResourceRegistry(),
        session.getConfigurationRegistry());
    assertTrue(handler.accepts(ServiceBindingConfig.class));
  }

  @Test
  public void shouldAcceptEditableServiceBindingConfig() {
    Session session = Session.getSession();
    ServiceBindingResourceGenerator handler = new ServiceBindingResourceGenerator(session.getResourceRegistry(),
        session.getConfigurationRegistry());
    assertTrue(handler.accepts(EditableServiceBindingConfig.class));
  }
}
