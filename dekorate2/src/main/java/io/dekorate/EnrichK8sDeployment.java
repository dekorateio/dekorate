///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.fabric8:kubernetes-client-api:7.8.0
//DEPS io.fabric8:kubernetes-model-apps:7.8.0
//SOURCES visitor/SetImageVisitor.java
//SOURCES visitor/AddLabelsVisitor.java

package io.dekorate;

import java.util.LinkedHashMap;
import java.util.Map;

import io.dekorate.visitor.AddLabelsVisitor;
import io.dekorate.visitor.SetImageVisitor;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;

public class EnrichK8sDeployment {

  public static void main(String[] args) {
    if (args.length < 1) {
      System.err
          .println("Usage: jbang dekorate2/src/main/java/io/dekorate/EnrichK8sDeployment.java <image-url> [key=value ...]");
      System.err.println(
          "Example: jbang dekorate2/src/main/java/io/dekorate/EnrichK8sDeployment.java quay.io/myorg/myapp:1.0 env=prod team=backend");
      System.exit(1);
    }

    String image = args[0];

    Map<String, String> labels = new LinkedHashMap<>();
    for (int i = 1; i < args.length; i++) {
      String[] kv = args[i].split("=", 2);
      if (kv.length == 2) {
        labels.put(kv[0], kv[1]);
      }
    }

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

    // This is where we define the visitors
    builder.accept(new SetImageVisitor(image));
    if (!labels.isEmpty()) {
      builder.accept(new AddLabelsVisitor(labels));
    }

    Deployment deployment = builder.build();
    System.out.println(Serialization.asYaml(deployment));
  }
}
