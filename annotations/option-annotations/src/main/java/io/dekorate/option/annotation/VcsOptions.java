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

package io.dekorate.option.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.dekorate.kubernetes.config.Configuration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
@Pojo(name = "VcsConfig", relativePath = "../config", autobox = true, mutable = true, superClass = Configuration.class, withStaticBuilderMethod = false, withStaticAdapterMethod = false, adapter = @Adapter(suffix = "Adapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface VcsOptions {

  /**
   * The remote to use.
   * 
   * @return the name of the remote, defaults to 'origin'.
   */
  String remote() default "origin";

  /**
   * Flag that specifies that https is preferred.
   * When use vcs url that use 'git+ssh' will be converted to https.
   * 
   * @return true, if https is prefered.
   */
  boolean httpsPreferred() default false;
}
