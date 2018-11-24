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

package io.ap4k.examples.svcat;


import io.ap4k.annotation.KubernetesApplication;
import io.ap4k.servicecatalog.annotation.ServiceCatalogInstance;
import io.ap4k.servicecatalog.annotation.ServiceCatalog;

@KubernetesApplication
@ServiceCatalog(instances = @ServiceCatalogInstance(name = "mysql-instance", serviceClass = "apb-mysql", servicePlan = "default"))
public class Main {
}
