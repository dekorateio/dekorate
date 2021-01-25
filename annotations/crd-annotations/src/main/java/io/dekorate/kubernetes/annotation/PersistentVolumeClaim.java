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
package io.dekorate.kubernetes.annotation;

import io.dekorate.kubernetes.config.ApplicationConfiguration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
@Pojo(name = "PersistentVolumeClaimConfig", mutable = true, superClass = ApplicationConfiguration.class, relativePath = "../config", withStaticAdapterMethod = false, adapter = @Adapter(relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PersistentVolumeClaim {

  /*
   * The name of the claim.
   * 
   * @return the name, or 'source' if no name specified.
   */
  String name() default "";

  /*
   * The size requirement of the generated PVC in gigs.
   * This only makes sense for generated PVCs.
   * 
   * @return the size, or 1Gi (default).
   */
  int size() default 1;

  /*
   * The unit (e.g. Ki, Mi, Gi) of the generated PVC for the source workspace.
   * 
   * @return The unit, defaults in Gi.
   */
  String unit() default "Gi";

  /*
   * The storage class requirement of the generated PVC
   * This only makes sense for generated PVCs.
   * 
   * @return the storage class or standard (default).
   */
  String storageClass() default "standard";

  AccessMode accessMode() default AccessMode.ReadWriteOnce;

  /*
   * The labels to use as matchLabels in the generated PVC selector.
   * 
   * @return
   */
  Label[] matchLabels() default {};

}
