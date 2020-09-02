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

package io.dekorate.utils;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.utils.Exec.ProjectExec;

public class Maven {

  public static String MVN = "mvn";
  public static String MVNW = "mvnw";
  public static String DASH_VERSION = "-version";

  public static String NEW_LINE = "[\\n\\r]+";
  public static String SPACE = " ";

  public static String FALLBACK_MAVEN_VERSION = "3.6.3";

  private static final Logger LOGGER = LoggerFactory.getLogger();
  
  public static String getVersion(Path modulePath) {
    Path moduleMvnw = modulePath.resolve(MVNW);
    Path rootMvnw = Git.getRoot(modulePath).orElse(modulePath).resolve(MVNW);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ProjectExec exec = Exec.inPath(modulePath).redirectingOutput(out);

    boolean success = false;
    if  (moduleMvnw.toFile().exists()) {
      success = exec.commands(moduleMvnw.toAbsolutePath().toString(), DASH_VERSION);
    } else if  (rootMvnw.toFile().exists()) {
       success = exec.commands(rootMvnw.toAbsolutePath().toString(), DASH_VERSION);
    } else {
      success = exec.commands(MVN, DASH_VERSION);
    }

    if (!success) {
      return FALLBACK_MAVEN_VERSION;
    }

    return getVersionFromOutput(new String(out.toByteArray()));
  }


  private static String getVersionFromOutput(String output) {
    if (Strings.isNullOrEmpty(output)) {
      LOGGER.warning("Unknown maven version output format. Expected at least one line. Falling back to: " + FALLBACK_MAVEN_VERSION + "!");
      return FALLBACK_MAVEN_VERSION;
    }

    Optional<String> versionLine = Arrays.stream(output.split(NEW_LINE))
      .filter(l -> l.startsWith("Apache Maven"))
      .findFirst();

    if (!versionLine.isPresent()) {
      LOGGER.warning("Unknown maven version output format. Expected at least one line. Falling back to: " + FALLBACK_MAVEN_VERSION + "!");
      return FALLBACK_MAVEN_VERSION;

    }
    String[] parts = versionLine.map(l -> l.split(SPACE)).get();
    if (parts.length < 3) {
      LOGGER.warning("Unknown maven version output format. Expected 'Apache Maven x.y.z ...'. Falling back to: " + FALLBACK_MAVEN_VERSION + "!");
      return FALLBACK_MAVEN_VERSION;
     }
    return parts[2];
  }
}
