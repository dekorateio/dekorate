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
package io.dekorate.examples.svcat;

import io.dekorate.deps.kubernetes.api.builder.TypedVisitor;
import io.dekorate.deps.kubernetes.api.model.EnvFromSourceFluent;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.api.model.apps.Deployment;
import io.dekorate.deps.kubernetes.api.model.apps.DeploymentBuilder;
import io.dekorate.deps.servicecatalog.api.model.ServiceBinding;
import io.dekorate.deps.servicecatalog.api.model.ServiceInstance;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ServiceCatalogExampleTest {

 @Test
  public void shouldContainServiceInstanceAndBinding() {
   KubernetesList list = Serialization.unmarshal(ServiceCatalogExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
   assertNotNull(list);
   assertTrue(findFirst(list, ServiceInstance.class).isPresent());
   assertTrue(findFirst(list, ServiceBinding.class).isPresent());
   Optional<Deployment> deployment = findFirst(list, Deployment.class);
   assertTrue(deployment.isPresent());

   AtomicBoolean hasBindingEnv = new AtomicBoolean(false);
   new DeploymentBuilder(deployment.get()).accept(new TypedVisitor<EnvFromSourceFluent>() {
     @Override
     public void visit(EnvFromSourceFluent env) {
       if (env.hasSecretRef()) {
         hasBindingEnv.set(true);
       }
     }
   });
   assertTrue(hasBindingEnv.get());
 }

   <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
