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
package io.dekorate.servicecatalog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
@Pojo(relativePath = "../config", mutable = true, withStaticBuilderMethod = false, withStaticAdapterMethod = false, adapter = @Adapter(suffix = "Adapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface ServiceCatalogInstance {

  /**
   * The name of the {@link me.snowdrop.servicecatalog.api.model.ServiceInstance} to generate.
   * 
   * @return The name.
   */
  String name();

  /**
   * The external name of the {@link me.snowdrop.servicecatalog.api.model.ClusterServiceClass}.
   * 
   * @return The external name.
   */
  String serviceClass();

  /**
   * The external name of the {@link me.snowdrop.servicecatalog.api.model.ClusterServicePlan}.
   * 
   * @return The external name.
   */
  String servicePlan();

  /**
   * An array of {@link Parameter} that is used to describe {@link me.snowdrop.servicecatalog.api.model.ServiceInstance}
   * parameters.
   * 
   * @return The array.
   */
  Parameter[] parameters() default {};

  /**
   * The name of the secret to use inside the {@link me.snowdrop.servicecatalog.api.model.ServiceBinding}.
   * When this is used, a {@link me.snowdrop.servicecatalog.api.model.ServiceBinding} will be generated for the target
   * {@link me.snowdrop.servicecatalog.api.model.ServiceInstance}.
   * The generated binding will use the specified secret.
   * 
   * @return The name of the secret.
   */
  String bindingSecret() default "";
}
