/**
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class SanitizeRemoteUrlTest {
  @ParameterizedTest(name = "{0} should be sanitized to {1}")
  @CsvSource({
      "git@github.com:myorg/myproject.git, https://github.com/myorg/myproject.git",
      "https://github.com/myorg/myproject.git, https://github.com/myorg/myproject.git",
      "git+ssh://git@github.com/halkyonio/operator, https://github.com/halkyonio/operator.git",
      "https://gitlab.com/foo/bar.git, https://gitlab.com/foo/bar.git",
      "git@gitlab.com:foo/bar.git, https://gitlab.com/foo/bar.git",
      "git+ssh://git@gitlab.com/foo/bar.git, https://gitlab.com/foo/bar.git",
  })
  void sanitizeRemoteUrlShouldWork(String original, String expected) {
    assertEquals(expected, Git.sanitizeRemoteUrl(original));
  }
}
