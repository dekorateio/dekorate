/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package io.dekorate.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class GitTest {

  private static final ClassLoader loader = GitTest.class.getClassLoader();
  private static final String CONFIG_SUFFIX = "/git/config";
  private static final String GIT_SIMPLE = "git-simple";
  private static final String GIT_SSH = "git-ssh";
  private static final String GIT_GITLAB = "git-gitlab";
  private static final Map<String, Path> configurationNameToConfigRoot = new HashMap<>(7);

  @BeforeAll
  static void setup() {
    setup(GIT_SIMPLE);
    setup(GIT_SSH);
    setup(GIT_GITLAB);
  }

  private static Path getDotGit(File configFile) {
    return configFile.toPath().getParent().getParent().resolve(Git.DOT_GIT);
  }

  private static Path getRoot(File configFile) {
    return configFile.toPath().getParent().getParent();
  }

  static void setup(String gitConfig) {
    final URL configURL = loader.getResource(gitConfig + CONFIG_SUFFIX);
    final File configFile = Urls.toFile(configURL);
    configFile.toPath().getParent().toFile().renameTo(getDotGit(configFile).toFile());
    configurationNameToConfigRoot.put(gitConfig, getRoot(configFile));
  }

  @ParameterizedTest
  @ValueSource(strings = { GIT_SIMPLE, GIT_SSH })
  void shouldDetectRoot(String configFile) {
    final Path root = getRootFor(configFile);
    final Path detected = Git.getRoot(root).orElse(null);
    assertEquals(root, detected);
  }

  @ParameterizedTest(name = "{0} should have \"{1}\" as remote url")
  @CsvSource({ GIT_SIMPLE + ", git@github.com:myorg/myproject.git" })
  void shouldGetRemoteUrl(String configFile, String expected) throws Exception {
    final Path root = getRootFor(configFile);
    Optional<String> repoUrl = Git.getRemoteUrl(root, Git.ORIGIN);
    assertNotNull(repoUrl);
    assertTrue(repoUrl.isPresent());
    assertEquals(expected, repoUrl.get());
  }

  private Path getRootFor(String configFile) {
    final Path root = configurationNameToConfigRoot.get(configFile);
    if (root == null) {
      fail("No configuration named '" + configFile
          + "' exist. Did you properly set it up in @BeforeAll annotated method?");
    }
    return root;
  }

  @ParameterizedTest(name = "{0} should have \"{1}\" as safe remote url")
  @CsvSource({
      GIT_SIMPLE + ", https://github.com/myorg/myproject.git",
      GIT_GITLAB + ", https://gitlab.com/foo/bar.git"
  })
  void shouldGetSafeRemoteUrl(String configFile, String expected) throws Exception {
    final Path root = getRootFor(configFile);
    Optional<String> repoUrl = Git.getSafeRemoteUrl(root, Git.ORIGIN);
    assertNotNull(repoUrl);
    assertTrue(repoUrl.isPresent());
    assertEquals(expected, repoUrl.get());
  }

  @ParameterizedTest(name = "{0} should have \"{1}\" as branch")
  @CsvSource({ GIT_SIMPLE + ", master", GIT_SSH + ", extract-api" })
  void shouldGetBranch(String configFile, String expected) throws Exception {
    final Path root = getRootFor(configFile);
    Optional<String> branch = Git.getBranch(root);
    assertNotNull(branch);
    assertTrue(branch.isPresent());
    assertEquals(expected, branch.get());
  }

  @ParameterizedTest(name = "{0} should have \"{1}\" as commit sha")
  @CsvSource({ GIT_SIMPLE + ", myawesomegitsha" })
  void shouldGetCommitSHA(String configFile, String expected) throws Exception {
    final Path root = getRootFor(configFile);
    Optional<String> sha = Git.getCommitSHA(root);
    assertNotNull(sha);
    assertTrue(sha.isPresent());
    assertEquals(expected, sha.get());
  }

  @ParameterizedTest(name = "should read remotes map from \"{0}\"")
  @ValueSource(strings = { GIT_SIMPLE })
  void shouldGetCommitSHA(String configFile) throws Exception {
    final Path root = getRootFor(configFile);
    Map<String, String> remotes = Git.getRemotes(root);
    assertNotNull(remotes);
    assertFalse(remotes.isEmpty());
  }
}
