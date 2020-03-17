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

public class Jvm {

  private static final String JAVA_VERSION_KEY = "java.version";
  private static final String DOT = "[\\._\\-]+";

  public static int getVersion() {
    String version = System.getProperty(JAVA_VERSION_KEY);
    if (Strings.isNullOrEmpty(version)) {
      throw new IllegalStateException("System property: " +JAVA_VERSION_KEY + " is not defined!");
    }

    String[] parts = version.trim().split(DOT);
    if (parts.length == 0) {
      throw new IllegalStateException("Java version: " + version + " uses an unknown format!");
     
    }

    if(parts[0].equals("1")) {
        version = parts[2];
    } else {
      version = parts[1];
    }
    return Integer.parseInt(version);
  }
}
