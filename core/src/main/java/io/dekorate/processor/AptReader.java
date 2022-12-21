/**
 * Copyright 2018 The original authors.
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
package io.dekorate.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

import io.dekorate.Session;
import io.dekorate.SessionReader;
import io.dekorate.WithProject;
import io.dekorate.utils.Serialization;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.KubernetesResource;

public class AptReader implements SessionReader, WithProject {

  private static final String INPUT_FILE_INCLUDE_REGEX = "^(.*?)\\.(yml|yaml)$";
  private static final String INPUT_FILE_EXCLUDE_REGEX = "^.*?-cr\\.(yml|yaml)$";
  private static final String COMMON = "common";

  private final ProcessingEnvironment processingEnv;
  private final Pattern inputFileIncludePattern;
  private final Pattern inputFileExcludePattern;

  AptReader(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
    this.inputFileIncludePattern = Pattern.compile(INPUT_FILE_INCLUDE_REGEX);
    this.inputFileExcludePattern = Pattern.compile(INPUT_FILE_EXCLUDE_REGEX);
  }

  @Override
  public void read(Session session) {
    if (canRead()) {
      listInputFiles().stream().map(this::read).filter(Objects::nonNull).map(Map::entrySet).flatMap(Set::stream)
          .forEach(e -> e.getValue().getItems().forEach(item -> {
            if (COMMON.equals(e.getKey())) {
              session.getResourceRegistry().common().addToItems(item);
            } else {
              session.getResourceRegistry().add(e.getKey(), item);
            }
          }));
    }
  }

  private File getInputDir() {
    return getProject().getBuildInfo().getResourceDir().resolve(getProject().getDekorateInputDir()).toFile();
  }

  private boolean canRead() {
    return projectExists() && Strings.isNotNullOrEmpty(getProject().getDekorateInputDir()) && getInputDir().exists()
        && getInputDir().isDirectory();
  }

  private List<File> listInputFiles() {
    final File[] filteredInputFiles = getInputDir()
        .listFiles(((dir, name) -> inputFileIncludePattern.matcher(name).matches()
            && !inputFileExcludePattern.matcher(name).matches()));
    return Arrays.asList(Optional.ofNullable(filteredInputFiles).orElse(new File[0]));
  }

  private Map<String, KubernetesList> read(File file) {
    try (InputStream is = new FileInputStream(file)) {
      final Matcher fileNameMatcher = inputFileIncludePattern.matcher(file.getName());
      final String name = fileNameMatcher.find() ? fileNameMatcher.group(1) : "invalid-name";
      final KubernetesResource resource = Serialization.unmarshal(is, KubernetesResource.class);
      if (resource instanceof KubernetesList) {
        return Collections.singletonMap(name, (KubernetesList) resource);
      } else if (resource instanceof HasMetadata) {
        final KubernetesListBuilder klb = new KubernetesListBuilder();
        klb.addToItems((HasMetadata) resource);
        return Collections.singletonMap(name, klb.build());
      }
    } catch (IOException e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, file.getAbsolutePath() + " not found.");
    }
    return null;
  }
}
