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

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

public class FileProjectFactory {

  private static Project PROJECT = null;
  public static final String GITHUB_SSH = "git@github.com:";
  public static final String GITHUB_HTTPS = "https://github.com/";

  /**
   * Creates a {@link Project} from the specified {@link ProcessingEnvironment}.
   * @param file          A file within the project.
   * @return              The project.
   */
  public static Project create(File file) {
    if (PROJECT != null) {
      return PROJECT;
    }
    synchronized (FileProjectFactory.class) {
      if (PROJECT == null) {
        PROJECT = createInternal(file);
      }
    }
    return PROJECT;
  }

  private static Project createInternal(File f) {
    Path path = f.toPath();
    Optional<BuildInfo> info = getProjectInfo(path);
    Optional<ScmInfo> scmInfo = getScmInfo();
    while (path != null && !info.isPresent()) {
      path = path.getParent();
      info = getProjectInfo(path);

    }
    return new Project(path, info.orElseThrow(() -> new IllegalStateException("Could not find matching project info read")), scmInfo.get());
  }

  /**
   * Read the {@link BuildInfo} from the specified path.
   * @param path  The path.
   * @return      An {@link Optional} {@link BuildInfo}.
   */
  private static Optional<BuildInfo> getProjectInfo(Path path) {
    if (path == null) {
      return Optional.empty();
    }

    return StreamSupport.stream(ServiceLoader.load(BuildInfoReader.class, FileProjectFactory.class.getClassLoader()).spliterator(), false)
      .filter(r -> r.isApplicable(path))
      .sorted(Comparator.comparingInt(BuildInfoReader::order))
      .findFirst()
      .map(r -> r.getInfo(path));
  }

  private static Optional<ScmInfo> getScmInfo(){
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    Optional<ScmInfo> scmInfoOpt = Optional.empty();
    try {
      Repository repo = builder
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build();
      String uri = repo.getConfig().getString("remote", "origin", "url");
      uri = uri.replace(GITHUB_SSH, GITHUB_HTTPS);
      String branch = repo.getBranch();
      scmInfoOpt = Optional.of(new ScmInfo(uri, branch, ""));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return scmInfoOpt;
  }

}
