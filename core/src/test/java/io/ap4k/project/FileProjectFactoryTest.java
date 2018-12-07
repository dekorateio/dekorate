package io.ap4k.project;

import io.ap4k.utils.Urls;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileProjectFactoryTest {

  String GRADLE_WITH_POM = "gradle/gradle-with-pom/build.gradle";

  @Test
  void shouldPrioritizeGradleOverMaven() {
    URL gradleWithPom = GradleInfoReaderTest.class.getClassLoader().getResource(GRADLE_WITH_POM);
    File file = Urls.toFile(gradleWithPom);
    Path root = file.toPath().getParent();
    Project project = FileProjectFactory.create(file);
    assertNotNull(project);
    BuildInfo info = project.getBuildInfo();
    assertNotNull(info);
    assertEquals("gradle", info.getName());
    assertEquals("jar", info.getPackaging());
    assertNull(info.getVersion());
  }
}
