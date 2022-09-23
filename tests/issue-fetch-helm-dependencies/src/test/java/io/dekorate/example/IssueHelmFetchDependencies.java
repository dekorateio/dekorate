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

package io.dekorate.example;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class IssueHelmFetchDependencies {

  private static final String CHART_NAME = "issue-fetch-deps";

  @Test
  public void shouldFetchDependencies() throws FileNotFoundException {
    assertTrue(Stream.of(Paths.get("target", "helm", "kubernetes").toFile().listFiles())
        .anyMatch(f -> f.getName().startsWith(CHART_NAME) && f.getName().endsWith(".tar.gz")));

    assertNotNull(getResourceAsStream("charts/postgresql-11.6.22.tgz"));
  }

  private final InputStream getResourceAsStream(String file) throws FileNotFoundException {
    return new FileInputStream(Paths.get("target", "helm", "kubernetes").resolve(CHART_NAME).resolve(file).toFile());
  }
}
