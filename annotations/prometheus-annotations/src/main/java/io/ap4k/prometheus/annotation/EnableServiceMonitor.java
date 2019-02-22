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
package io.ap4k.prometheus.annotation;

import io.ap4k.kubernetes.config.Configuration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.ap4k.deps.kubernetes.api.builder")
@Pojo(name = "ServiceMonitorConfig", mutable = true, superClass = Configuration.class, relativePath = "../config", withStaticAdapterMethod = false, adapter = @Adapter(name = "ServiceMonitorConfigAdapter", relativePath = "../adapter", withMapAdapterMethod = true))
public @interface EnableServiceMonitor {
  String port() default "http";
  String path() default "/metrics";
  int interval() default 10;
  boolean honorLabels() default false;
}
