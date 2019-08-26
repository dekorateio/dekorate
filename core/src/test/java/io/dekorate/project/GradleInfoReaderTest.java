/**
 * Copyright 2018 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.project;

import io.dekorate.utils.Urls;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GradleInfoReaderTest {
  String GRADLE_INITIAL = "gradle/gradle-initial/build.gradle";
  String GRADLE_VERSIONED = "gradle/gradle-versioned/build.gradle";
  String GRADLE_WITH_SETTINGS = "gradle/gradle-with-settings/build.gradle";

  @Test
  void shouldParsePlainBuildGradle() {
    URL gradleInitial = GradleInfoReaderTest.class.getClassLoader().getResource(GRADLE_INITIAL);
    File file = Urls.toFile(gradleInitial);
    Path root = file.toPath().getParent();
    BuildInfo info = new GradleInfoReader().getInfo(root);
    assertNotNull(info);
    assertEquals("gradle-initial", info.getName());
    assertEquals("jar", info.getPackaging());
    assertNull(info.getVersion());
  }

  @Test
  void shouldParseVersionedBuildGradle() {
    URL gradleInitial = GradleInfoReaderTest.class.getClassLoader().getResource(GRADLE_VERSIONED);
    File file = Urls.toFile(gradleInitial);
    Path root = file.toPath().getParent();
    BuildInfo info = new GradleInfoReader().getInfo(root);
    assertNotNull(info);
    assertEquals("myartifact", info.getName());
    assertEquals("jar", info.getPackaging());
  }

  @Test
  void shouldParseBuildGradleWithSettings() {
    URL gradleInitial = GradleInfoReaderTest.class.getClassLoader().getResource(GRADLE_WITH_SETTINGS);
    File file = Urls.toFile(gradleInitial);
    Path root = file.toPath().getParent();
    BuildInfo info = new GradleInfoReader().getInfo(root);
    assertNotNull(info);
    assertEquals("with-settings", info.getName());
  }
}
