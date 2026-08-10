package io.dekorate.kubernetes.decorator;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.dekorate.kubernetes.config.EditableEnv;
import io.dekorate.kubernetes.config.EnvBuilder;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvFromSource;

class AddEnvVarDecoratorTest {

  @Test
  void shouldAddPrefixToContainerWhenUsingConfigMapRef() {

    EditableEnv editableEnv = new EnvBuilder()
        .withConfigmap("configs")
        .withPrefix("ALPHA")
        .build();

    AddEnvVarDecorator addEnvVarDecorator = new AddEnvVarDecorator("jdeployment", "jcontainer", editableEnv);

    ContainerBuilder containerBuilder = new ContainerBuilder();

    addEnvVarDecorator.andThenVisit(containerBuilder);

    List<EnvFromSource> envFromSources = containerBuilder.buildEnvFrom();

    Assertions.assertThat(envFromSources.size()).isOne();

    Assertions.assertThat(envFromSources)
        .filteredOn("prefix", "ALPHA")
        .extracting("configMapRef.name")
        .contains("configs")
        .isNotEmpty();

  }

  @Test
  void shouldAddPrefixToContainerWhenUsingSecretRef() {

    EditableEnv editableEnv = new EnvBuilder()
        .withSecret("secrets")
        .withPrefix("ALPHA")
        .build();

    AddEnvVarDecorator addEnvVarDecorator = new AddEnvVarDecorator("jdeployment", "jcontainer", editableEnv);

    ContainerBuilder containerBuilder = new ContainerBuilder();

    addEnvVarDecorator.andThenVisit(containerBuilder);

    List<EnvFromSource> envFromSources = containerBuilder.buildEnvFrom();

    Assertions.assertThat(envFromSources.size()).isOne();

    Assertions.assertThat(envFromSources)
        .filteredOn("prefix", "ALPHA")
        .extracting("secretRef.name")
        .contains("secrets")
        .isNotEmpty();
  }
}
