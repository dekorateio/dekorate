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
package io.ap4k.istio.annotation;

import io.ap4k.kubernetes.config.Configuration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Buildable(builderPackage = "io.ap4k.deps.kubernetes.api.builder")
@Pojo(name = "IstioConfig", superClass = Configuration.class, relativePath = "../config", withStaticAdapterMethod = false, adapter = @Adapter(relativePath = "../adapter"))
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Istio {

  String version() default "1.0.0";

  ProxyConfig proxyConfig() default @ProxyConfig();
}
