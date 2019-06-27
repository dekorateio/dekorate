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
package io.ap4k.project;

import io.ap4k.utils.Urls;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BazelInfoReaderTest {

  String BAZEL_INIT = "bazel/bazel-initial/BUILD";
  String BAZEL_VERSIONED = "bazel/bazel-versioned/BUILD";

  @Test
  void shouldParsePlainBuild() {
    URL bazelInitial = BazelInfoReaderTest.class.getClassLoader().getResource(BAZEL_INIT);
    File file = Urls.toFile(bazelInitial);
    Path root = file.toPath().getParent();
    BuildInfo info = new BazelInfoReader().getInfo(root);
    assertNotNull(info);
    assertEquals("bazel-initial", info.getName());
    assertEquals("jar", info.getPackaging());
    assertNull(info.getVersion());
  }


  @Test
  void shouldParseVersionedBuild() {
    URL bazelVersioned = BazelInfoReaderTest.class.getClassLoader().getResource(BAZEL_VERSIONED);
    File file = Urls.toFile(bazelVersioned);
    Path root = file.toPath().getParent();
    BuildInfo info = new BazelInfoReader().getInfo(root);
    assertNotNull(info);
    assertEquals("bazel-versioned", info.getName());
    assertEquals("0.0.1", info.getVersion());
    assertEquals("jar", info.getPackaging());
  }
}
