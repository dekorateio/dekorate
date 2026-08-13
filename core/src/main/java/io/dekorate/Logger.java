/**
 * Copyright 2018 The ordinal authors.
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

package io.dekorate;

public interface Logger {

  static enum Level {
    OFF, ERROR, WARN, INFO, DEBUG;
  }

  String THRESHOLD = "io.dekorate.log.level";

  default Level getLogLevel() {
    String threshold = System.getProperty(THRESHOLD, "INFO");
    try {
      return Enum.valueOf(Level.class, threshold.toUpperCase());
    } catch (IllegalArgumentException e) {
      return Level.INFO;
    }
  }

  default boolean isDebugEnabled() {
    return getLogLevel().ordinal() >= Level.DEBUG.ordinal();
  }

  default boolean isInfoEnabled() {
    return getLogLevel().ordinal() >= Level.INFO.ordinal();
  }

  default boolean isWarnEnabled() {
    return getLogLevel().ordinal() >= Level.WARN.ordinal();
  }

  default boolean isErrorEnabled() {
    return getLogLevel().ordinal() >= Level.ERROR.ordinal();
  }

  void debug(String message);

  void info(String message);

  default void info(String message, Object... objects) {
    info(String.format(message, objects));
  }

  void warning(String message);

  void error(String message);
}
