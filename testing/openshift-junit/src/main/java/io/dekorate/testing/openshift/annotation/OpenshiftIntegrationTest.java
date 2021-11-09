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
package io.dekorate.testing.openshift.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import io.dekorate.testing.openshift.OpenshiftExtension;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
@Pojo(name = "OpenshiftIntegrationTestConfig", relativePath = "../config", mutable = true, withStaticBuilderMethod = false,

    withStaticAdapterMethod = false, adapter = @Adapter(suffix = "Adapter", relativePath = "../adapter"))
@Target({ TYPE, METHOD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@ExtendWith(OpenshiftExtension.class)
public @interface OpenshiftIntegrationTest {

  /**
   * Flag to define whether the extension should automatically apply resources.
   * 
   * @return True, if extension should automatically deploy dekorate generated resources.
   */
  boolean deployEnabled() default true;

  /**
   * Flag to define whether the extension should automatically apply resources.
   * 
   * @return True, if extensions should automatically perform container builds.
   */
  boolean buildEnabled() default true;

  /**
   * Flag to define whether the extension should automatically push image.
   * 
   * @return True, if extension should automatically push the image.
   */
  boolean pushEnabled() default false;

  /**
   * The amount of time in seconds to wait for the image stream tags to be available.
   * 
   * @return The max amount in milliseconds.
   */
  long imageStreamTagTimeout() default 120000;

  /**
   * The amount of time in milliseconds to wait for application to become ready.
   * 
   * @return The max amount in milliseconds.
   */
  long readinessTimeout() default 300000;

  /**
   * List of additional modules to be loaded by the test framework.
   *
   * @return The list of additional modules to be loaded.
   */
  String[] additionalModules() default {};
}
