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

/**
 * References:
 * - https://www.baeldung.com/jvm-parameters (awesome resource).
 */
@Buildable(builderPackage = "io.dekorate.deps.kubernetes.api.builder")
@Pojo(name = "JvmConfig", relativePath = "../config",
      mutable = true,
      superClass = Configuration.class,
      withStaticBuilderMethod = false,
      withStaticAdapterMethod = false,
      adapter = @Adapter(suffix = "Adapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface JvmOptions {

  /**
   * Starting heap size in megabytes.
   * @return  The starting heap size in megabytes, or 0 if undefined.
   */
  int xms() default 0;


  /**
   * Maxium heap size in megabytes.
   * @return  The maximum heap size in megabytes, or 0 if undefined.
   */
  int xmx() default 0;

  /**
   * Server Flag.
   * @return True if server flag is used.
   */
  boolean server() default false;

  /**
   * String deduplication flag.
   * @return True if string deduplication is enabled.
   */
  boolean useStringDeduplication() default false;

  /**
   * Prefer IPv4 stack.
   * @return True if preferred.
   */
  boolean preferIPv4Stack() default false;
  /**
   * Instructs the JVM to dump heap into physical file in case of OutOfMemoryError
   * @return True, if enabled.
   */
  boolean heapDumpOnOutOfMemoryError() default false;

  /**
   * Is a policy that limits the proportion of the VM’s time that is spent in GC before an OutOfMemory error is thrown.
   * @return True if enabled.
   */
  boolean useGCOverheadLimit() default false;


  /**
   * Garbage Collector implementation.
   * @return  The gc impl or Undefined if none is selected.
   */
  GarbageCollector gc() default GarbageCollector.Undefined;

  /**
   * The Secure random source to use.
   * This will determine -Djava.security.egd option.
   * @return  The source to use. Defaults to undefined.
   */
  SecureRandomSource secureRandom() default SecureRandomSource.Undefined;

}
