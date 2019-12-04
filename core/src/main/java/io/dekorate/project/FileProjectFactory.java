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

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import io.dekorate.utils.Git;

import static io.dekorate.project.Project.DEFAULT_DEKORATE_OUTPUT_DIR;

public class FileProjectFactory {

  private static final String INPUT_DIR = "dekorate.input.dir";
  private static final String OUTPUT_DIR = "dekorate.output.dir";

  private static Project PROJECT = null;


  /**
   * Creates a {@link Project} from the specified {@link File}.
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
    Path infoPath = f.toPath();
    Optional<BuildInfo> info = getProjectInfo(infoPath);
    while (infoPath != null && !info.isPresent()) {
      infoPath = infoPath.getParent();
      info = getProjectInfo(infoPath);
    }

    Path scmPath = f.toPath();
    while (scmPath != null && !scmPath.resolve(Git.DOT_GIT).toFile().exists()) {
      scmPath = scmPath.getParent();
    }
    Optional<ScmInfo> scmInfo = getScmInfo(scmPath);
    return new Project(infoPath, System.getProperty(INPUT_DIR), System.getProperty(OUTPUT_DIR, DEFAULT_DEKORATE_OUTPUT_DIR), info.orElseThrow(() -> new IllegalStateException("Could not find matching project info read")), scmInfo.orElse(null));
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

  private static Optional<ScmInfo> getScmInfo(Path path) {
    Optional<ScmInfo> scmInfo = Optional.empty();
    if (path == null) {
      return scmInfo;
    }
    String url = Git.getSafeRemoteUrl(path, Git.ORIGIN).orElse(null);
    String branch = Git.getBranch(path).orElse(null);
    scmInfo = Optional.of(new ScmInfo(path, url, branch, ""));
    return scmInfo;
  }

}
