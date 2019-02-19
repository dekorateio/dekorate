/*
 * Copyright (C) 2018 Red Hat inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ap4k.prometheus.client;


import io.ap4k.deps.kubernetes.client.Config;
import io.ap4k.deps.kubernetes.client.ResourceHandler;
import io.ap4k.deps.kubernetes.client.Watch;
import io.ap4k.deps.kubernetes.client.Watcher;
import io.ap4k.deps.okhttp3.OkHttpClient;
import io.ap4k.prometheus.model.ServiceMonitor;
import io.ap4k.prometheus.model.ServiceMonitorBuilder;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class ServiceMonitorHandler implements ResourceHandler<ServiceMonitor, ServiceMonitorBuilder> {

  @Override
  public String getKind() {
    return ServiceMonitor.class.getSimpleName();
  }

  @Override
  public ServiceMonitor create(OkHttpClient client, Config config, String namespace, ServiceMonitor item) {
    return new ServiceMonitorOperationImpl(client, config, "monitoring.coreos.com", "v1", namespace, null, true, item, null, false, -1, new TreeMap<String, String>(), new TreeMap<String, String>(), new TreeMap<String, String[]>(), new TreeMap<String, String[]>(), new TreeMap<String, String>()).create();
  }

  @Override
  public ServiceMonitor replace(OkHttpClient client, Config config, String namespace, ServiceMonitor item) {
    return new ServiceMonitorOperationImpl(client, config, "monitoring.coreos.com", "v1", namespace, null, true, item, null, true, -1, new TreeMap<String, String>(), new TreeMap<String, String>(), new TreeMap<String, String[]>(), new TreeMap<String, String[]>(), new TreeMap<String, String>()).replace(item);
  }

  @Override
  public ServiceMonitor reload(OkHttpClient client, Config config, String namespace, ServiceMonitor item) {
    return new ServiceMonitorOperationImpl(client, config, "monitoring.coreos.com", "v1", namespace, null, true, item, null, false, -1, new TreeMap<String, String>(), new TreeMap<String, String>(), new TreeMap<String, String[]>(), new TreeMap<String, String[]>(), new TreeMap<String, String>()).fromServer().get();
  }

  @Override
  public ServiceMonitorBuilder edit(ServiceMonitor item) {
    return new ServiceMonitorBuilder(item);
  }

  @Override
  public Boolean delete(OkHttpClient client, Config config, String namespace, ServiceMonitor item) {
    return new ServiceMonitorOperationImpl(client, config, "monitoring.coreos.com", "v1", namespace, null, true, item, null, false, -1, new TreeMap<String, String>(), new TreeMap<String, String>(), new TreeMap<String, String[]>(), new TreeMap<String, String[]>(), new TreeMap<String, String>()).delete(item);
  }

  @Override
  public Watch watch(OkHttpClient client, Config config, String namespace, ServiceMonitor item, Watcher<ServiceMonitor> watcher) {
    return new ServiceMonitorOperationImpl(client, config, "monitoring.coreos.com", "v1", namespace, null, true, item, null, false, -1, new TreeMap<String, String>(), new TreeMap<String, String>(), new TreeMap<String, String[]>(), new TreeMap<String, String[]>(), new TreeMap<String, String>()).watch(watcher);
  }

  @Override
  public Watch watch(OkHttpClient client, Config config, String namespace, ServiceMonitor item, String resourceVersion, Watcher<ServiceMonitor> watcher) {
    return new ServiceMonitorOperationImpl(client, config, "monitoring.coreos.com", "v1", namespace, null, true, item, null, false, -1, new TreeMap<String, String>(), new TreeMap<String, String>(), new TreeMap<String, String[]>(), new TreeMap<String, String[]>(), new TreeMap<String, String>()).watch(resourceVersion, watcher);
  }

  @Override
  public ServiceMonitor waitUntilReady(OkHttpClient client, Config config, String namespace, ServiceMonitor item, long amount, TimeUnit timeUnit) throws InterruptedException {
    return new ServiceMonitorOperationImpl(client, config, "monitoring.coreos.com", "v1", namespace, null, true, item, null, false, -1, new TreeMap<String, String>(), new TreeMap<String, String>(), new TreeMap<String, String[]>(), new TreeMap<String, String[]>(), new TreeMap<String, String>()).waitUntilReady(amount, timeUnit);
  }
}
