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
package io.dekorate.option.annotation;

import io.dekorate.kubernetes.config.Configuration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
@Pojo(name = "GeneratorConfig", relativePath = "../config",
      mutable = true,
      superClass = Configuration.class,
      withStaticBuilderMethod = false,
      withStaticAdapterMethod = false,
      adapter = @Adapter(suffix = "Adapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface GeneratorOptions {

  /**
   * The path to input manifests.
   * If the path is specified and the manifests are found, they will be used as input to the generator process.
   * In this case, the instead of generating resources from scratch, the existing will be used and will be decarated
   * according to the annotation configuration.
   * @return  The path, or empty string.
   */
  String inputPath() default "";


  /**
   * The output path where the generated/decorated manifests will be stored.
   * @return  The path, or empty string.
   */
  String outputPath() default "";
}
