package io.dekorate.certmanager.manfiest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dekorate.Session;
import io.dekorate.WithProject;
import io.dekorate.certmanager.config.CertificateConfig;
import io.dekorate.certmanager.config.EditableCertificateConfig;
import io.dekorate.certmanager.manifest.CertificateResourceGenerator;
import io.dekorate.project.FileProjectFactory;

public class CertificateResourceGeneratorTest {

  @BeforeAll
  public static void setup() throws IOException {
    Path tempDir = Files.createTempDirectory("dekorate");
    WithProject withProject = new WithProject() {
    };
    withProject.setProject(FileProjectFactory.create(new File(".")).withDekorateOutputDir(tempDir.toAbsolutePath().toString())
        .withDekorateMetaDir(tempDir.toAbsolutePath().toString()));
  }

  @Test
  public void shouldAcceptConfig() {
    Session session = Session.getSession();
    CertificateResourceGenerator handler = new CertificateResourceGenerator(session.getResourceRegistry(),
        session.getConfigurationRegistry());
    assertTrue(handler.accepts(CertificateConfig.class));
  }

  @Test
  public void shouldAcceptEditableConfig() {
    Session session = Session.getSession();
    CertificateResourceGenerator handler = new CertificateResourceGenerator(session.getResourceRegistry(),
        session.getConfigurationRegistry());
    assertTrue(handler.accepts(EditableCertificateConfig.class));
  }
}
