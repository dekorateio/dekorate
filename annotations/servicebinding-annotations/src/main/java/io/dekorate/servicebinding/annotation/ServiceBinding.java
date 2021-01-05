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
 **/

package io.dekorate.servicebinding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.dekorate.kubernetes.annotation.Env;
import io.dekorate.kubernetes.config.ApplicationConfiguration;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.dekorate.deps.kubernetes.api.builder")
@Pojo(name = "ServiceBindingConfig", mutable = true, superClass = ApplicationConfiguration.class, relativePath = "../config", withStaticAdapterMethod = false)
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface ServiceBinding {

  String name() default "";

  Application application() default @Application;

  Service[] services() default {};

  String envVarPrefix() default "";

  boolean detectBindingResources() default false;

  boolean bindAsFiles() default false;

  String mountPath() default "";

  Env[] customEnvVar() default {};

  BindingPath bindingPath() default @BindingPath;

}
