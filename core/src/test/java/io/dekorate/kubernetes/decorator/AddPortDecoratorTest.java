
package io.dekorate.kubernetes.decorator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.dekorate.kubernetes.config.PortBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;

class AddPortDecoratorTest {

  /**
   * The purpose of this test is to ensure that its possible visit ports that have no name and optionally match them by numnber.
   */
  @Test
  public void shouldMatchPortByNumber() throws Exception {
    AddPortDecorator addName = new AddPortDecorator(new PortBuilder().withName("http").withContainerPort(80).build());

    Deployment deployment = new DeploymentBuilder()
        .withNewMetadata().withName("my-deployment").endMetadata()
        .withNewSpec().withNewTemplate().withNewSpec()
        .addNewContainer().withName("my-container").withImage("busybox").addNewPort().withContainerPort(80).endPort()
        .endContainer()
        .endSpec().endTemplate().endSpec()
        .accept(addName)
        .build();

    Container c = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
    ContainerPort p = c.getPorts().get(0);
    assertEquals(1, c.getPorts().size());
    assertEquals("http", p.getName());
    assertEquals(80, p.getContainerPort());
  }
}
