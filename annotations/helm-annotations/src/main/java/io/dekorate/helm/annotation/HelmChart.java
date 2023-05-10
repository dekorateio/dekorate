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

package io.dekorate.helm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.dekorate.kubernetes.config.Configuration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
@Pojo(name = "HelmChartConfig", autobox = true, mutable = true, superClass = Configuration.class, relativePath = "../config", withStaticBuilderMethod = true, withStaticAdapterMethod = false, adapter = @Adapter(name = "HelmChartConfigAdapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface HelmChart {

  boolean enabled() default true;

  String name();

  String home() default "";

  String[] sources() default {};

  String version() default "";

  String description() default "";

  String[] keywords() default {};

  Maintainer[] maintainers() default {};

  String icon() default "";

  String apiVersion() default "v2";

  String condition() default "";

  String tags() default "";

  String appVersion() default "";

  boolean deprecated() default false;

  Annotation[] annotations() default {};

  String kubeVersion() default "";

  HelmDependency[] dependencies() default {};

  String type() default "";

  String valuesRootAlias() default "app";

  String valuesProfileSeparator() default ".";

  boolean createTarFile() default false;

  boolean createValuesSchemaFile() default true;

  boolean createReadmeFile() default true;

  String extension() default "tar.gz";

  String tarFileClassifier() default "";

  String notes() default "/NOTES.template.txt";

  String inputFolder() default "helm";

  String outputFolder() default "helm";

  ValueReference[] values() default {};

  ValuesSchema valuesSchema() default @ValuesSchema;

  HelmExpression[] expressions() default {};

  AddIfStatement[] addIfStatements() default {};
}
