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

public class ScmInfo {

  private Path root;
  private String url;
  private String branch;
  private String commit;

  public ScmInfo() {
  }

  public ScmInfo(Path root, String url, String branch, String commit) {
    this.root=root;
    this.url = url;
    this.branch = branch;
    this.commit = commit;
  }

  /**
   * @return the root
   */
  public Path getRoot() {
    return root;
  }

  /**
   * @param root the root to set
   */
  public void setRoot(Path root) {
    this.root = root;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getCommit() {
    return commit;
  }

  public void setCommit(String commit) {
    this.commit = commit;
  }
}
