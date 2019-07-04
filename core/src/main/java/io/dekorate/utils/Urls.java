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
package io.dekorate.utils;

import java.io.File;
import java.net.URL;

public class Urls {

  /**
   * Convert a {@URL} pointing to a local file or resource to an actual {@File}.
   * @param url The url.
   * @return    The file that the url points at, or the file that contains the resource if its contained in a jar etc.
   */
  public static File toFile(URL url) {
    String path = url.getPath();
    if  (path.contains("!")) {
      path = path.substring(0, path.indexOf("!"));
    }
    if (path.startsWith("jar:")) {
      path = path.substring(4);
    }
    if (path.startsWith("file:")) {
      path = path.substring(5);
    }
    return new File(path);
  }
}
