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
 **/
package io.dekorate.project;

import java.nio.file.Path;
import java.util.Map;

import io.sundr.builder.annotations.Buildable;

public class ScmInfo {

  private final Path root;
  private final String branch;
  private final String commit;
  private final Map<String, String> remote;

  public ScmInfo() {
    this(null, null, null, null);
  }

  @Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
  public ScmInfo(Path root, Map<String, String> remote, String branch, String commit) {
    this.root = root;
    this.remote = remote;
    this.branch = branch;
    this.commit = commit;
  }

  public ScmInfoBuilder edit() {
    return new ScmInfoBuilder(this);
  }

  public Map<String, String> getRemote() {
    return remote;
  }

  /**
   * @return the root
   */
  public Path getRoot() {
    return root;
  }

  public String getBranch() {
    return branch;
  }

  public String getCommit() {
    return commit;
  }

}
