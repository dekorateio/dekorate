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
package io.ap4k.prometheus.client.dsl.internal;

import io.ap4k.deps.kubernetes.client.Config;
import io.ap4k.deps.kubernetes.client.dsl.Resource;
import io.ap4k.deps.kubernetes.client.dsl.base.HasMetadataOperation;
import io.ap4k.deps.kubernetes.client.dsl.NonNamespaceOperation;
import io.ap4k.deps.kubernetes.client.utils.ApiVersionUtil;
import io.ap4k.deps.kubernetes.client.dsl.base.OperationContext;

import io.ap4k.deps.okhttp3.OkHttpClient;

import io.ap4k.prometheus.model.DoneableServiceMonitor;
import io.ap4k.prometheus.model.ServiceMonitor;
import io.ap4k.prometheus.model.ServiceMonitorList;
import io.ap4k.prometheus.model.DoneableServiceMonitor;

import java.util.Map;
import java.util.TreeMap;


public class ServiceMonitorOperationsImpl extends HasMetadataOperation<ServiceMonitor, ServiceMonitorList, DoneableServiceMonitor, Resource<ServiceMonitor, DoneableServiceMonitor>> {

  public ServiceMonitorOperationsImpl(OkHttpClient client, Config config) {
    this(new OperationContext().withOkhttpClient(client).withConfig(config));
  }

  public ServiceMonitorOperationsImpl(OperationContext context) {
    super(context.withApiGroupName("monitoring.coreos.om")
    .withApiGroupVersion("v1")
    .withPlural("servicemonitors"));
    this.type = ServiceMonitor.class;
    this.listType = ServiceMonitorList.class;
    this.doneableType = DoneableServiceMonitor.class;
  }

  public ServiceMonitorOperationsImpl newInstance(OperationContext context) {
    return new ServiceMonitorOperationsImpl(context);
  }
}
