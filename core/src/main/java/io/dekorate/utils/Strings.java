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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.dekorate.DekorateException;

public class Strings {

  private static final char COMMA = ',';

  public static boolean isNotNullOrEmpty(String str) {
    return !isNullOrEmpty(str);
  }

  public static boolean isNullOrEmpty(String str) {
    return str == null || str.isEmpty();
  }

  public static String join(final Object[] array) {
    return join(array, ',');
  }

  public static String join(final Object[] array, final char separator) {
    if (array == null) {
      return null;
    }
    if (array.length == 0) {
      return "";
    }
    final StringBuilder buf = new StringBuilder();
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        buf.append(separator);
      }
      if (array[i] != null) {
        buf.append(array[i]);
      }
    }
    return buf.toString();
  }

  public static String read(Path path) {
    try {
      return new String(Files.readAllBytes(path));
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
  }
 
}
