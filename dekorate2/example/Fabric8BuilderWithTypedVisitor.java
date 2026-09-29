///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS io.fabric8:kubernetes-client-api:7.8.0
//DEPS io.fabric8:kubernetes-model-apps:7.8.0
//DEPS org.jboss.logging:jboss-logging:3.6.3.Final
//DEPS org.jboss.logmanager:jboss-logmanager:3.2.2.Final
//DEPS io.dekorate:dekorate-2-core:999-SNAPSHOT
//DEPS io.dekorate:dekorate-2-kubernetes:999-SNAPSHOT

// USE SOURCES ./core/src/main/java/io/dekorate/core/Configuration.java
// USE SOURCES ./core/src/main/java/io/dekorate/core/VisitorFactory.java
// USE SOURCES ./core/src/main/java/io/dekorate/core/VisitorRegistry.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/deployment/SetImageDecorate.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/common/AddLabelsDecorate.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/common/AddAnnotationsDecorate.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/common/SetNameDecorate.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/common/SetNamespaceDecorate.java

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.dekorate.core.Configuration;
import io.dekorate.core.VisitorRegistry;
import io.dekorate.generator.kubernetes.common.AddLabelsDecorate;
import io.dekorate.generator.kubernetes.deployment.SetImageDecorate;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;

public class Fabric8BuilderWithTypedVisitor {

  public static void main(String[] args) throws IOException {
    System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");

    if (args.length < 1) {
      System.err.println("Usage: jbang Fabric8BuilderWithTypedVisitor.java <config-file>");
      System.err.println("Example: jbang Fabric8BuilderWithTypedVisitor.java application.properties");
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

    VisitorRegistry registry = new VisitorRegistry(config)
        .register(new SetImageDecorate<>())
        .register(new AddLabelsDecorate<>());
    registry.applyAll(builder);

    Deployment deployment = builder.build();
    System.out.println(Serialization.asYaml(deployment));
  }
}
