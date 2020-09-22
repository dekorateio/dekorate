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

package io.dekorate.option.generator;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import io.dekorate.Generator;
import io.dekorate.WithProject;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.option.adapter.GeneratorConfigAdapter;
import io.dekorate.option.config.GeneratorConfig;
import io.dekorate.option.config.GeneratorConfigBuilder;
import io.dekorate.processor.SimpleFileWriter;
import io.dekorate.utils.Strings;

public interface OptionsGenerator extends Generator, WithProject {

  String OPTIONS = "options";
  String INPUT_DIR = "dekorate.input.dir";
  String OUTPUT_DIR = "dekorate.output.dir";

  @Override
  default String getKey() {
    return OPTIONS;
  }

  @Override
  default Class<? extends Configuration> getConfigType() {
    return GeneratorConfig.class;
  }

  @Override
  default void addAnnotationConfiguration(Map map) {
    on(new AnnotationConfiguration<>(GeneratorConfigAdapter.newBuilder(propertiesMap(map, GeneratorConfig.class))));
  }

  @Override
  default void addPropertyConfiguration(Map map) {
    on(new PropertyConfiguration<>(GeneratorConfigAdapter.newBuilder(propertiesMap(map, GeneratorConfig.class))));
  }

  default void on(ConfigurationSupplier<GeneratorConfig> config) {
    GeneratorConfig c = config.get();
    configurePaths(c.getInputPath(), c.getOutputPath());
  }

  default void configurePaths(String defaultInputPath, String defaultOutputPath) {
    final String inputPath = System.getProperty(INPUT_DIR, defaultInputPath);
    final String outputPath = Optional.ofNullable(System.getProperty(OUTPUT_DIR)).map(path -> {
      resolve(path).mkdirs();
      return path;
    }).orElse(defaultOutputPath);
    if (isInputPathValid(inputPath)) {
      applyToProject(p -> p.withDekorateInputDir(inputPath));
      getSession().configurators().add(new ConfigurationSupplier<>(new GeneratorConfigBuilder()));
    }

    if (isOutputPathValid(outputPath)) {
      applyToProject(p -> p.withDekorateOutputDir(outputPath));
      getSession().setWriter(new SimpleFileWriter(
          getProject().getBuildInfo().getClassOutputDir().resolve(getProject().getDekorateMetaDir()),
          resolve(outputPath).toPath()));
    }
  }

  default boolean isInputPathValid(String path) {
    return Strings.isNotNullOrEmpty(path) && resolve(path).exists();
  }

  default boolean isOutputPathValid(String path) {
    return Strings.isNotNullOrEmpty(path) && (resolve(path).exists() || resolve(path).mkdirs());
  }

  default File resolve(String unixPath) {
    return new File(getProject().getBuildInfo().getClassOutputDir().toFile(), unixPath.replace('/', File.separatorChar));
  }
}
