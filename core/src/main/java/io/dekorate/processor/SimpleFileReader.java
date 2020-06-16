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
package io.dekorate.processor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Session;
import io.dekorate.SessionReader;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.dekorate.utils.Serialization;

public class SimpleFileReader implements SessionReader {

  private final Logger LOGGER = LoggerFactory.getLogger();

  private static final String DOT_YML = ".yml";
  private static final String DOT_JSON = ".json";

  private final Path path;
  private final Set<String> targets;

  public SimpleFileReader(Path path, Set<String> targets) {
    this.path = path;
    this.targets = targets;
  }

  @Override
  public void read(Session session) {
    LOGGER.info("Checking for existing resources in: " + path.toAbsolutePath().normalize().toString() + ".");
    findApplicableResources().forEach((k, v) -> {
        v.getItems().forEach(i -> {
            LOGGER.info("Adding existing " + i.getKind() + " with name: " + i.getMetadata().getName() + ".");
            session.resources().add(k, i);
          });
      });
  }

  private Path pathToJson(String target) {
    return path.resolve(target + DOT_JSON);
  }

  private Path pathToYml(String target) {
    return path.resolve(target + DOT_YML);
  }

  private boolean hasYml(String target) {
    return pathToYml(target).toFile().exists();
  }

  private boolean hasJson(String target) {
    return pathToJson(target).toFile().exists();
  }

  private KubernetesList readYml(String target) {
    try (InputStream is = new FileInputStream(pathToYml(target).toFile())) {
      return Serialization.unmarshalAsList(is);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read kubernetes resources from: " + path.toAbsolutePath().normalize().toString(), e);
    }
  }

  private KubernetesList readJson(String target) {
    try (InputStream is = new FileInputStream(pathToJson(target).toFile())) {
      return Serialization.unmarshalAsList(is);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read kubernetes resources from: " + path.toAbsolutePath().normalize().toString(), e);
    }
  }

  private Map<String, KubernetesList> findApplicableResources() {
    Map<String, KubernetesList> result = new HashMap<>();
    if (!path.toFile().exists() || !path.toFile().isDirectory()) {
      return result;
    }
    Map<String, KubernetesList> ymlResourcePaths = targets.stream()
      .filter(this::hasYml)
      .collect(Collectors.toMap(t -> t, this::readYml));

    Map<String, KubernetesList> jsonResourcePaths = targets.stream()
      .filter(this::hasJson)
      .collect(Collectors.toMap(t -> t, this::readJson));

    result.putAll(jsonResourcePaths);
    result.putAll(ymlResourcePaths);
    return result;
  }

}
