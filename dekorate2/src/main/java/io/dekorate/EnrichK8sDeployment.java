///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.fabric8:kubernetes-client-api:7.8.0
//DEPS io.fabric8:kubernetes-model-apps:7.8.0
//SOURCES core/Configuration.java
//SOURCES visitor/SetImageVisitor.java
//SOURCES visitor/AddLabelsVisitor.java

package io.dekorate;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.dekorate.core.Configuration;
import io.dekorate.visitor.AddLabelsVisitor;
import io.dekorate.visitor.SetImageVisitor;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;

public class EnrichK8sDeployment {

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.err.println("Usage: jbang EnrichK8sDeployment.java <config-file>");
      System.err.println("Example: jbang EnrichK8sDeployment.java application.properties");
      System.exit(1);
    }

    Path configPath = Paths.get(args[0]).toAbsolutePath();
    Configuration config = Configuration.fromProperties(configPath);

    DeploymentBuilder builder = new DeploymentBuilder()
        .withNewMetadata()
        .withName("my-app")
        .withNamespace("default")
        .endMetadata()
        .withNewSpec()
        .withReplicas(1)
        .withNewSelector()
        .addToMatchLabels("app", "my-app")
        .endSelector()
        .withNewTemplate()
        .withNewMetadata()
        .addToLabels("app", "my-app")
        .endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName("app")
        .withImage("placeholder:latest")
        .endContainer()
        .endSpec()
        .endTemplate()
        .endSpec();

    // Place to define the visitor's classes
    builder.accept(new SetImageVisitor<Configuration>(config));
    builder.accept(new AddLabelsVisitor<Configuration>(config));

    Deployment deployment = builder.build();
    System.out.println(Serialization.asYaml(deployment));
  }
}
