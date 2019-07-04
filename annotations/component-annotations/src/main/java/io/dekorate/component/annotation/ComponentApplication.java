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
package io.dekorate.component.annotation;


import io.dekorate.component.model.DeploymentMode;
import io.dekorate.kubernetes.annotation.BuildConfig;
import io.dekorate.kubernetes.annotation.Env;
import io.dekorate.kubernetes.config.Configuration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Buildable(builderPackage = "io.dekorate.deps.kubernetes.api.builder")
@Pojo(name = "ComponentConfig", mutable = true, superClass = Configuration.class, relativePath = "../config", withStaticAdapterMethod = false,
       adapter = @Adapter(suffix = "Adapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface ComponentApplication {

  String name() default "";

  DeploymentMode deploymentMode() default DeploymentMode.dev;

  boolean exposeService() default false;

  Env[] envs() default {};

  BuildConfig buildconfig() default @BuildConfig;
}
