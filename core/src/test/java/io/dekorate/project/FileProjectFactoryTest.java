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
