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

class ProjectParseAppResourceFileTest {

    private static final String APPLICATION_PROPERTIES = "application.properties";
    private static final String APPLICATION_YAML = "application.yaml";

    @TempDir
    Path tempPath;

    @Test
    void shouldParsePropertiesFile() throws Exception {
        URI applicationPropertiesURI = ProjectParseAppResourceFileTest.class.getClassLoader().getResource(APPLICATION_PROPERTIES).toURI();
        Files.move(Paths.get(applicationPropertiesURI), tempPath.resolve(APPLICATION_PROPERTIES));

        Project project = new Project(null, new BuildInfo(null, null, null, null, null, tempPath));

        Map<String, Object> result = project.parseAppResourceFile(APPLICATION_PROPERTIES);
        assertThat(result).containsOnlyKeys("key1", "key2", "k1")
                .contains(entry("key1", "value1"), entry("key2", "value2"));
        assertThat((Map)result.get("k1")).containsOnly(entry("k2", "v"));
    }

    @Test
    void shouldParseYamlFile() throws Exception {
        URI applicationYamlURI = ProjectParseAppResourceFileTest.class.getClassLoader().getResource(APPLICATION_YAML).toURI();
        Files.move(Paths.get(applicationYamlURI), tempPath.resolve(APPLICATION_YAML));

        Project project = new Project(null, new BuildInfo(null, null, null, null, null, tempPath));

        Map<String, Object> result = project.parseAppResourceFile(APPLICATION_YAML);
        assertThat(result).containsOnlyKeys("key1", "key2", "k1")
                .contains(entry("key1", "value1"), entry("key2", "value2"));
        assertThat((Map)result.get("k1")).containsOnly(entry("k2", "v"));
    }
}