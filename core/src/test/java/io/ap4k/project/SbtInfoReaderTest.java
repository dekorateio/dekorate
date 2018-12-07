package io.ap4k.project;


import io.ap4k.utils.Urls;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SbtInfoReaderTest {

  String SBT_INITIAL = "sbt/sbt-initial/build.sbt";
  String SBT_VERSIONED = "sbt/sbt-versioned/build.sbt";
  String systemScalaVersion = SbtInfoReader.getSystemScalaVersion();

  @Test
  void shouldReadSystemScalaVersion() {
    assertNotNull(systemScalaVersion);
  }

  @Test
  void shouldParsePlainBuildSbt() {
    URL sbtInitial = SbtInfoReader.class.getClassLoader().getResource(SBT_INITIAL);
    File file = Urls.toFile(sbtInitial);
    Path root = file.toPath().getParent();
    BuildInfo info = new SbtInfoReader().getInfo(root);
    assertNotNull(info);
    assertEquals("sbt-initial", info.getName());
    assertEquals("jar", info.getPackaging());
    assertEquals(SbtInfoReader.DEFAULT_VERSION, info.getVersion());

    String expectedOutputFile = root.resolve("target/").resolve(info.getName() + "_" + systemScalaVersion + "-" + info.getVersion() + ".jar").toAbsolutePath().toString();
    assertEquals(expectedOutputFile, info.getOutputFile().toAbsolutePath().toString());
  }

  @Test
  void shouldParseVersionedBuildSbt() {
    URL sbtVersioned = SbtInfoReader.class.getClassLoader().getResource(SBT_VERSIONED);
    File file = Urls.toFile(sbtVersioned);
    Path root = file.toPath().getParent();
    BuildInfo info = new SbtInfoReader().getInfo(root);
    assertNotNull(info);
    assertEquals("sbt-versioned", info.getName());
    assertEquals("jar", info.getPackaging());
    assertEquals("1.0.0", info.getVersion());

    String expectedOutputFile = root.resolve("target/").resolve(info.getName() + "_" + systemScalaVersion + "-" + info.getVersion() + ".jar").toAbsolutePath().toString();
    assertEquals(expectedOutputFile, info.getOutputFile().toAbsolutePath().toString());
  }
}
