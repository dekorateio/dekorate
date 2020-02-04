/**
 * Copyright 2019 The original authors.
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
package io.dekorate.thorntail.project;

import io.dekorate.project.BuildInfo;
import io.dekorate.project.BuildInfoBuilder;
import io.dekorate.project.MavenInfoReader;

import java.nio.file.Path;

public class ThorntailMavenBuildInfoReader extends MavenInfoReader {
  private static final String THORNTAIL_JAR = "-thorntail.jar";

  @Override
  public int order() {
    // we only need to modify `order`, so that this class comes sooner than generic MavenInfoReader
    //
    // we don't have to modify `isApplicable`, because this class is only present when the Thorntail support
    // is explicitly requested by the user
    return super.order() - 10;
  }

  @Override
  public BuildInfo getInfo(Path root) {
    BuildInfo result = super.getInfo(root);
    String fileName = result.getOutputFile().getFileName().toString();
    String uberjar = fileName.replace("." + result.getPackaging(), THORNTAIL_JAR);
    return result.edit().withOutputFile(result.getOutputFile().getParent().resolve(uberjar)).build();
  }
}
