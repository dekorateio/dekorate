package io.ap4k.micronaut;

import io.ap4k.WithSession;
import io.ap4k.Generator;
import io.ap4k.kubernetes.config.Port;
import io.ap4k.kubernetes.config.PortBuilder;
import io.ap4k.kubernetes.configurator.AddLivenessProbe;
import io.ap4k.kubernetes.configurator.AddPort;
import io.ap4k.kubernetes.configurator.AddReadinessProbe;

import java.util.Collections;
import java.util.Map;

public interface MicronautWebAnnotationGenerator extends Generator, WithSession {

  Map WEB_ANNOTATIONS=Collections.emptyMap();

  @Override
  default void add(Map map) {
    Port port = detectHttpPort();
    session.configurators().add(new AddPort(port));
    session.configurators().add(new AddReadinessProbe(port.getContainerPort()));
    session.configurators().add(new AddLivenessProbe(port.getContainerPort()));
  }

  default Port detectHttpPort()  {
    return new PortBuilder().withContainerPort(8080).withName("http").build();
  }

}
