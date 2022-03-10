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
package io.dekorate.prometheus.client.dsl.internal;

import io.dekorate.prometheus.model.ServiceMonitor;
import io.dekorate.prometheus.model.ServiceMonitorList;
import io.fabric8.kubernetes.client.Client;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperation;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.fabric8.kubernetes.client.dsl.internal.OperationContext;

public class ServiceMonitorOperationsImpl
    extends HasMetadataOperation<ServiceMonitor, ServiceMonitorList, Resource<ServiceMonitor>> {

  public ServiceMonitorOperationsImpl(Client client) {
    this(HasMetadataOperationsImpl.defaultContext(client));
  }

  public ServiceMonitorOperationsImpl(OperationContext context) {
    super(context.withApiGroupName("monitoring.coreos.com").withApiGroupVersion("v1").withPlural("servicemonitors"),
        ServiceMonitor.class, ServiceMonitorList.class);
  }

  public ServiceMonitorOperationsImpl newInstance(OperationContext context) {
    return new ServiceMonitorOperationsImpl(context);
  }
}
