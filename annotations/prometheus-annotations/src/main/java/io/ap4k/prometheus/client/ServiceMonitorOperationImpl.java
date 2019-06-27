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
package io.ap4k.prometheus.client;

import io.ap4k.deps.applicationcrd.api.client.util.ApiVersionUtil;
import io.ap4k.deps.kubernetes.client.Config;
import io.ap4k.deps.kubernetes.client.dsl.NonNamespaceOperation;
import io.ap4k.deps.kubernetes.client.dsl.Resource;
import io.ap4k.deps.kubernetes.client.dsl.base.HasMetadataOperation;
import io.ap4k.deps.okhttp3.OkHttpClient;
import io.ap4k.prometheus.model.DoneableServiceMonitor;
import io.ap4k.prometheus.model.ServiceMonitor;
import io.ap4k.prometheus.model.ServiceMonitorList;

import java.util.Map;
import java.util.TreeMap;


public class ServiceMonitorOperationImpl extends HasMetadataOperation<ServiceMonitor, ServiceMonitorList, DoneableServiceMonitor, Resource<ServiceMonitor, DoneableServiceMonitor>> {

    public ServiceMonitorOperationImpl(OkHttpClient client, Config config) {
        this(client, config, "monitoring.coreos.com", "v1", null, null, true, null, null, false, -1, new TreeMap<String, String>(), new TreeMap<String, String>(), new TreeMap<String, String[]>(), new TreeMap<String, String[]>(), new TreeMap<String, String>());
    }

    public ServiceMonitorOperationImpl(OkHttpClient client, Config config, String apiGroup, String apiVersion, String namespace, String name, Boolean cascading, ServiceMonitor item, String resourceVersion, Boolean reloadingFromServer, long gracePeriodSeconds, Map<String, String> labels, Map<String, String> labelsNot, Map<String, String[]> labelsIn, Map<String, String[]> labelsNotIn, Map<String, String> fields) {
        super(client, config, apiGroup, apiVersion, "servicemonitors", namespace, name, cascading, item, resourceVersion, reloadingFromServer, gracePeriodSeconds, labels, labelsNot, labelsIn, labelsNotIn, fields);
    }

    //Added for compatibility
    public ServiceMonitorOperationImpl(OkHttpClient client, Config config, String apiVersion, String namespace, String name, Boolean cascading, ServiceMonitor item, String resourceVersion, Boolean reloadingFromServer, long gracePeriodSeconds, Map<String, String> labels, Map<String, String> labelsNot, Map<String, String[]> labelsIn, Map<String, String[]> labelsNotIn, Map<String, String> fields) {
        super(client, config, ApiVersionUtil.apiGroup(item, apiVersion), ApiVersionUtil.apiVersion(item, apiVersion), "applications", namespace, name, cascading, item, resourceVersion, reloadingFromServer, gracePeriodSeconds, labels, labelsNot, labelsIn, labelsNotIn, fields);
    }

    @Override
    public NonNamespaceOperation<ServiceMonitor, ServiceMonitorList, DoneableServiceMonitor, Resource<ServiceMonitor, DoneableServiceMonitor>> inNamespace(String namespace) {
        return new ServiceMonitorOperationImpl(client, config, apiGroup, apiVersion, namespace, name, isCascading(), getItem(), getResourceVersion(), isReloadingFromServer(), getGracePeriodSeconds(), getLabels(), getLabelsNot(), getLabelsIn(), getLabelsNotIn(), getFields());
    }

    @Override
    public Resource<ServiceMonitor, DoneableServiceMonitor> withName(String name) {
        return new ServiceMonitorOperationImpl(client, config, apiGroup, apiVersion, namespace, name, isCascading(), getItem(), getResourceVersion(), isReloadingFromServer(), getGracePeriodSeconds(), getLabels(), getLabelsNot(), getLabelsIn(), getLabelsNotIn(), getFields());
    }

    @Override
    public boolean isResourceNamespaced() {
        return true;
    }

}
