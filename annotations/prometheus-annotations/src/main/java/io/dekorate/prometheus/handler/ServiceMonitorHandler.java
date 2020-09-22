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
package io.dekorate.prometheus.handler;

import io.dekorate.Handler;
import io.dekorate.Resources;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.prometheus.config.EditableServiceMonitorConfig;
import io.dekorate.prometheus.config.ServiceMonitorConfig;
import io.dekorate.prometheus.decorator.AddServiceMonitorResourceDecorator;

public class ServiceMonitorHandler implements Handler<ServiceMonitorConfig> {

  private final Resources resources;

  public ServiceMonitorHandler(Resources resources) {
    this.resources = resources;
  }

  @Override
  public int order() {
    return 450;
  }

  @Override
  public String getKey() {
    return "servicemonitor";
  }

  @Override
  public void handle(ServiceMonitorConfig config) {
    resources.decorate(new AddServiceMonitorResourceDecorator(config));
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(ServiceMonitorConfig.class) || type.equals(EditableServiceMonitorConfig.class);
  }
}
