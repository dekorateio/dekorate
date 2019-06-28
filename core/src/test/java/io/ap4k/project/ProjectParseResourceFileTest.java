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
package io.ap4k.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class ProjectParseResourceFileTest {

    private static final String APPLICATION_PROPERTIES = "application.properties";
    private static final String APPLICATION_YAML = "application.yaml";
    private static final String NON_EXISTENT_PROPERTIES = "nonExistent.properties";

    @TempDir
    Path tempPath;

    @Test
    void missingFile() {
        Project project = new Project(null, new BuildInfo(null, null, null, null, null, tempPath),null);

        Map<String, Object> result = project.parseResourceFile(NON_EXISTENT_PROPERTIES);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldParsePropertiesFile() throws Exception {
        URI applicationPropertiesURI = ProjectParseResourceFileTest.class.getClassLoader().getResource(APPLICATION_PROPERTIES).toURI();
        Files.move(Paths.get(applicationPropertiesURI), tempPath.resolve(APPLICATION_PROPERTIES));

        Project project = new Project(null, new BuildInfo(null, null, null, null, null, tempPath), null );

        Map<String, Object> result = project.parseResourceFile(APPLICATION_PROPERTIES);
        assertThat(result).containsOnlyKeys("key1", "key2", "k1")
                .contains(entry("key1", "value1"), entry("key2", "value2"));
        assertThat((Map)result.get("k1")).containsOnly(entry("k2", "v"));
    }

    @Test
    void shouldParseYamlFile() throws Exception {
        URI applicationYamlURI = ProjectParseResourceFileTest.class.getClassLoader().getResource(APPLICATION_YAML).toURI();
        Files.move(Paths.get(applicationYamlURI), tempPath.resolve(APPLICATION_YAML));

        Project project = new Project(null, new BuildInfo(null, null, null, null, null, tempPath),null);

        Map<String, Object> result = project.parseResourceFile(APPLICATION_YAML);
        assertThat(result).containsOnlyKeys("key1", "key2", "k1")
                .contains(entry("key1", "value1"), entry("key2", "value2"));
        assertThat((Map)result.get("k1")).containsOnly(entry("k2", "v"));
    }
}
