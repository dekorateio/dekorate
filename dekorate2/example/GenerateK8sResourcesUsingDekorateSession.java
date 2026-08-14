///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS io.fabric8:kubernetes-client-api:7.8.0
//DEPS io.fabric8:kubernetes-model-apps:7.8.0
//DEPS org.jboss.logging:jboss-logging:3.6.3.Final
//DEPS org.jboss.logmanager:jboss-logmanager:3.2.2.Final
//DEPS io.dekorate:dekorate-2-core:999-SNAPSHOT
//DEPS io.dekorate:dekorate-2-kubernetes:999-SNAPSHOT

// USE SOURCES ./core/src/main/java/io/dekorate/core/Configuration.java
// USE SOURCES ./core/src/main/java/io/dekorate/core/ConfigurationGenerator.java
// USE SOURCES ./core/src/main/java/io/dekorate/core/PropertiesConfigurationGenerator.java
// USE SOURCES ./core/src/main/java/io/dekorate/core/Generator.java
// USE SOURCES ./core/src/main/java/io/dekorate/core/VisitorFactory.java
// USE SOURCES ./core/src/main/java/io/dekorate/core/VisitorRegistry.java
// USE SOURCES ./core/src/main/java/io/dekorate/core/Session.java
// USE SOURCES ./core/src/main/java/io/dekorate/core/SessionListener.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/deployment/DeploymentGenerator.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/deployment/SetImageDecorate.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/service/ServiceGenerator.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/serviceaccount/ServiceAccountGenerator.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/rbac/RbacGenerator.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/secret/SecretGenerator.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/common/AddLabelsDecorate.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/common/AddAnnotationsDecorate.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/common/SetNameDecorate.java
// USE SOURCES ./kubernetes/src/main/java/io/dekorate/generator/kubernetes/common/SetNamespaceDecorate.java

import java.nio.file.Path;
import java.nio.file.Paths;

import io.dekorate.core.Generator;
import io.dekorate.core.PropertiesConfigurationGenerator;
import io.dekorate.core.Session;
import io.dekorate.core.SessionListener;
import io.dekorate.generator.kubernetes.deployment.DeploymentGenerator;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.utils.Serialization;

public class GenerateK8sResourcesUsingDekorateSession {

  public static void main(String[] args) {
    System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");

    if (args.length < 1) {
      System.err.println("Usage: jbang GenerateK8sResourcesUsingDekorateSession.java <config-file>");
      System.err.println("Example: jbang GenerateK8sResourcesUsingDekorateSession.java application.properties");
      System.exit(1);
    }

    Path configPath = Paths.get(args[0]).toAbsolutePath();
    Path exportPath = args.length > 1 ? Paths.get(args[1]) : Paths.get("target");

    try (Session session = new Session(new PropertiesConfigurationGenerator(configPath))) {
      session.withExportPath(exportPath);
      session.addListener(new SessionListener() {
        @Override
        public void onResourceGenerated(Generator generator, HasMetadata resource) {
          System.out.printf("Generated %s: %s/%s%n",
              resource.getKind(),
              resource.getMetadata().getNamespace(),
              resource.getMetadata().getName());
        }
      });

      session.load();
      session.register(new DeploymentGenerator());

      for (HasMetadata resource : session.start()) {
        System.out.println("---");
        System.out.print(Serialization.asYaml(resource));
      }
    }
  }
}
