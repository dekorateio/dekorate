package io.dekorate.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;

class ProjectSerializationTest {

  @Test
  public void shouldRoundTripProjectPreservingBuildInfo() {
    BuildInfo buildInfo = new BuildInfo("my-app", "1.0", "jar", "maven", "3.9.0",
        Paths.get("target/my-app.jar"), Paths.get("target/classes"), Paths.get("src/main/resources"));
    Project project = new Project(Paths.get("."), buildInfo, null);

    String yaml = Serialization.asYaml(project);
    Project restored = Serialization.unmarshal(yaml, Project.class);

    assertNotNull(restored.getBuildInfo(), "buildInfo must survive serialization round-trip");
    assertNotNull(restored.getBuildInfo().getClassOutputDir());
    assertTrue(restored.getBuildInfo().getClassOutputDir().toString().endsWith("target/classes"));
    assertEquals("maven", restored.getBuildInfo().getBuildTool());
  }
}
