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
 * 
 * 
 * 
**/

package io.dekorate.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Git {

  public static final String DOT_GIT = ".git";
  public static final String CONFIG = "config";
  public static final String ORIGIN = "origin";
  public static final String OB = "[";
  public static final String CB = "]";
  public static final String SLASH = "/";
  public static final String COLN = ":";
  public static final String EQUALS = "=";
  public static final String REMOTE = "remote";
  public static final String HEAD = "HEAD";
  public static final String URL = "url";
  public static final String REF = "ref";

  public static final String REMOTE_PATTERN = "^\\[remote \"([a-zA-Z0-9_-]+)\"\\]";

  /**
   * Get the git root.
   * 
   * @param path Any path under the target git repo.
   * @return The {@link Path} to the git root.
   */
  public static Optional<Path> getRoot(Path path) {
    Path root = path;
    while (root != null && !root.resolve(Git.DOT_GIT).toFile().exists()) {
      root = root.toAbsolutePath().getParent();
    }
    return Optional.ofNullable(root);
  }

  /**
   * Get the git config.
   * 
   * @param root the git root.
   * @return The {@link Path} to the git config.
   */
  public static Path getConfig(Path root) {
    return root.resolve(DOT_GIT).resolve(CONFIG);
  }

  public static Path getHead(Path root) {
    return root.resolve(DOT_GIT).resolve(HEAD);
  }

  /**
   * Get the git remote urls as a map.
   * 
   * @param path the path to the git config.
   * @return A {@link Map} of urls per remote.
   */
  public static Map<String, String> getRemotes(Path path) {
    Map<String, String> result = new HashMap<String, String>();
    try {
      final AtomicReference<String> currentRemote = new AtomicReference<>();
      Files.lines(getConfig(path)).map(String::trim).forEach(l -> {
          remoteValue(l).ifPresent(r -> currentRemote.set(r));
          if (l.startsWith(URL)  && l.contains(EQUALS)) {
            result.put(currentRemote.get(), l.split(EQUALS)[1].trim());
          }
        });
      return result;
    } catch (Exception e) {
      return result;
    }
  }
  


  /**
   * Get the git remote url.
   * 
   * @param path the path to the git config.
   * @param remote the remote.
   * @return The an {@link Optional} String with the URL of the specified remote.
   */
  public static Optional<String> getRemoteUrl(Path path, String remote) {
    return getRemoteUrl(path, remote, false);
  }

  public static Optional<String> getSafeRemoteUrl(Path path, String remote) {
    return getRemoteUrl(path, remote, true);
  }

  public static Optional<String> getRemoteUrl(Path path, String remote, boolean httpsPreferred) {
    try {
      Optional<String> url = Files.lines(getConfig(path)).map(String::trim)
          .filter(inRemote(remote, new AtomicBoolean()))
          .filter(l -> l.startsWith(URL) && l.contains(EQUALS))
          .map(s -> s.split(EQUALS)[1].trim())
          .findAny();
      return httpsPreferred ? url.map(Git::sanitizeRemoteUrl) : url;
    } catch (Exception e) {
      return Optional.empty();
    }
  }


  static String sanitizeRemoteUrl(String remoteUrl) {
    final int atSign = remoteUrl.indexOf('@');
    if (atSign > 0) {
      remoteUrl = remoteUrl.substring(atSign + 1);
      remoteUrl = remoteUrl.replaceFirst(":", "/");
      remoteUrl = "https://" + remoteUrl;
    }
    if (!remoteUrl.endsWith(".git")) {
      remoteUrl += ".git";
    }
    return remoteUrl;
  }

  /**
   * Get the git branch.
   * 
   * @param path the path to the git config.
   * @return The an {@link Optional} String with the branch.
   */
  public static Optional<String> getBranch(Path path) {
    try {
      return Files.lines(getHead(path)).map(String::trim)
          .filter(l -> l.startsWith(REF) && l.contains(SLASH))
          .map(s -> s.substring(s.lastIndexOf(SLASH) + 1).trim())
          .findAny();
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Get the git branch.
   * 
   * @param path the path to the git config.
   * @return The an {@link Optional} String with the branch.
   */
  public static Optional<String> getCommitSHA(Path path) {
    try {
      return Files.lines(getHead(path)).map(String::trim)
          .filter(l -> l.startsWith(REF) && l.contains(COLN))
          .map(s -> s.substring(s.lastIndexOf(COLN) + 1).trim())
          .map(ref -> path.resolve(DOT_GIT).resolve(ref))
          .filter(ref -> ref.toFile().exists())
          .map(Strings::read)
          .map(String::trim)
          .findAny();
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Create a predicate function that tracks if the a line is defined in the specified remote section.
   * 
   * @param remote The target remote.
   * @param state An atomic boolean which holds the predicate state.
   * @reuturn The predicate.
   */
  public static Predicate<String> inRemote(String remote, AtomicBoolean state) {
    return l -> {
      if (l.startsWith(OB) && l.contains(REMOTE) && l.contains(remote) && l.endsWith(CB)) {
        state.set(true);
      } else if (l.startsWith(OB) && l.endsWith(CB)) {
        state.set(false);
      }
      return state.get();
    };
  }


  public static Optional<String> remoteValue(String line) {
    Pattern p = Pattern.compile(REMOTE_PATTERN);
    Matcher m = p.matcher(line);
    if (m.matches()) {
      return Optional.of(m.group());
    } else {
      return Optional.empty();
    }
  }
}
