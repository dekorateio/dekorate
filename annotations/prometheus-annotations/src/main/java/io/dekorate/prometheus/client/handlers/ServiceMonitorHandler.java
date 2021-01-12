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
package io.dekorate.prometheus.client.handlers;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import io.dekorate.prometheus.client.dsl.internal.ServiceMonitorOperationsImpl;
import io.dekorate.prometheus.model.ServiceMonitor;
import io.dekorate.prometheus.model.ServiceMonitorBuilder;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ResourceHandler;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import okhttp3.OkHttpClient;

public class ServiceMonitorHandler implements ResourceHandler<ServiceMonitor, ServiceMonitorBuilder> {

  @Override
  public String getKind() {
    return ServiceMonitor.class.getSimpleName();
  }

  @Override
  public String getApiVersion() {
    return "monitoring.coreos.com/v1";
  }

  @Override
  public ServiceMonitor create(OkHttpClient client, Config config, String namespace, ServiceMonitor item) {
    return new ServiceMonitorOperationsImpl(client, config).withItem(item).inNamespace(namespace).create();
  }

  @Override
  public ServiceMonitor replace(OkHttpClient client, Config config, String namespace, ServiceMonitor item) {
    return new ServiceMonitorOperationsImpl(client, config).withItem(item).inNamespace(namespace)
        .withName(item.getMetadata().getName()).replace(item);
  }

  @Override
  public ServiceMonitor reload(OkHttpClient client, Config config, String namespace, ServiceMonitor item) {
    return new ServiceMonitorOperationsImpl(client, config).withItem(item).inNamespace(namespace)
        .withName(item.getMetadata().getName()).fromServer().get();
  }

  @Override
  public ServiceMonitorBuilder edit(ServiceMonitor item) {
    return new ServiceMonitorBuilder(item);
  }

  @Override
  public Boolean delete(OkHttpClient client, Config config, String namespace, DeletionPropagation propagationPolicy, ServiceMonitor item) {
      return new ServiceMonitorOperationsImpl(client, config).withItem(item).withPropagationPolicy(propagationPolicy).delete();
  }

  @Override
  public Watch watch(OkHttpClient client, Config config, String namespace, ServiceMonitor item,
      Watcher<ServiceMonitor> watcher) {
    return new ServiceMonitorOperationsImpl(client, config).withItem(item).inNamespace(namespace)
        .withName(item.getMetadata().getName()).watch(watcher);
  }

  @Override
  public Watch watch(OkHttpClient client, Config config, String namespace, ServiceMonitor item, String resourceVersion,
      Watcher<ServiceMonitor> watcher) {
    return new ServiceMonitorOperationsImpl(client, config).withItem(item).inNamespace(namespace).withName(item.getMetadata().getName()).watch(resourceVersion, watcher);
  }

 @Override
 public Watch watch(OkHttpClient client, Config config, String namespace, ServiceMonitor item, ListOptions listOptions, Watcher<ServiceMonitor> watcher) {
    return new ServiceMonitorOperationsImpl(client, config).withItem(item).inNamespace(namespace).withName(item.getMetadata().getName()).watch(listOptions, watcher);
 }

  @Override
  public ServiceMonitor waitUntilReady(OkHttpClient client, Config config, String namespace, ServiceMonitor item, long amount,
      TimeUnit timeUnit) throws InterruptedException {
    return new ServiceMonitorOperationsImpl(client, config).withItem(item).inNamespace(namespace)
        .withName(item.getMetadata().getName()).waitUntilReady(amount, timeUnit);
  }

  @Override
  public ServiceMonitor waitUntilCondition(OkHttpClient client, Config config, String namespace, ServiceMonitor item,
      Predicate<ServiceMonitor> condition, long amount, TimeUnit timeUnit) throws InterruptedException {
    return new ServiceMonitorOperationsImpl(client, config).withItem(item).inNamespace(namespace)
        .withName(item.getMetadata().getName()).waitUntilCondition(condition, amount, timeUnit);
  }


}
