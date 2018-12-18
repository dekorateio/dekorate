package io.ap4k.examples.svcat;

import io.ap4k.deps.kubernetes.api.builder.TypedVisitor;
import io.ap4k.deps.kubernetes.api.model.EnvFromSourceFluent;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.apps.Deployment;
import io.ap4k.deps.kubernetes.api.model.apps.DeploymentBuilder;
import io.ap4k.deps.servicecatalog.api.model.ServiceBinding;
import io.ap4k.deps.servicecatalog.api.model.ServiceInstance;
import io.ap4k.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ServiceCatalogExampleTest {

 @Test
  public void shouldContainServiceInstanceAndBinding() {
   KubernetesList list = Serialization.unmarshal(ServiceCatalogExampleTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/kubernetes.yml"));
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
