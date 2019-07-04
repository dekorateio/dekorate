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
 * 
 * 
 * 
 * 
**/

package io.ap4k.utils;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GitTest {

  static String GIT_CONFIG = "git-simple/git/config";
  static URL CONFIG_URL = GitTest.class.getClassLoader().getResource(GIT_CONFIG);
  static File CONFIG_FILE = Urls.toFile(CONFIG_URL);
  static Path DOT_GIT = CONFIG_FILE.toPath().getParent().getParent().resolve(Git.DOT_GIT);
  static Path ROOT = CONFIG_FILE.toPath().getParent().getParent();

  @BeforeAll
  public static void setup() {
    CONFIG_FILE.toPath().getParent().toFile().renameTo(DOT_GIT.toFile());
  }

  @Test
  public void shouldDetectRoot() {
    Path root = Git.getRoot(CONFIG_FILE.toPath());
    assertEquals(ROOT, root);
  }

  @Test
  public void shouldGetUrl() throws Exception {
    Optional<String> repoUrl = Git.getRemoteUrl(ROOT, Git.ORIGIN);
    assertNotNull(repoUrl);
    assertTrue(repoUrl.isPresent());
    assertEquals("git@github.com:myorg/myproject.git", repoUrl.get());
  }

  @Test
  public void shouldGetBranch() throws Exception {
    Optional<String> branch = Git.getBranch(ROOT);
    assertNotNull(branch);
    assertTrue(branch.isPresent());
    assertEquals("master", branch.get());
  }

  @Test
  public void shouldGetCommitSHA() throws Exception {
    Optional<String> sha = Git.getCommitSHA(ROOT);
    assertNotNull(sha);
    assertTrue(sha.isPresent());
    assertEquals("myawesomegitsha", sha.get());
  }
}
