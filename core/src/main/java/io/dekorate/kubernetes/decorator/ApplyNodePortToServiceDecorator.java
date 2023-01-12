package io.dekorate.kubernetes.decorator;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.utils.Ports;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ServicePortFluent;

public class ApplyNodePortToServiceDecorator extends NamedResourceDecorator<ServicePortFluent> {

  private final Optional<Port> port;

  public ApplyNodePortToServiceDecorator(BaseConfig config, Port[] ports, String defaultPort) {
    super(config.getName());

    port = findPort(ports, Ports.getPortByFilter(p -> p.getName().equals(defaultPort), config));
  }

  @Override
  public void andThenVisit(ServicePortFluent servicePort, ObjectMeta resourceMeta) {
    if (!port.isPresent()) {
      return;
    }

    if (port.get().getName().equals(servicePort.getName())) {
      if (hasNodePort(port.get())) {
        // if user already specified a concrete node port, we use it.
        servicePort.withNodePort(port.get().getNodePort());
      } else {
        // Otherwise, we calculate a node port based on the service name.
        servicePort.withNodePort(Ports.calculateNodePort(getName(), port.get()));
      }
    }
  }

  private Optional<Port> findPort(Port[] ports, Optional<Port> defaultPort) {
    if (ports != null) {
      if (ports.length == 1) {
        // If there is only one port, we use it
        return Optional.of(ports[0]);
      } else {
        // if the user provided multiple ports but only set node port on one, we use it
        List<Port> portsWithNodePort = Arrays.stream(ports).filter(this::hasNodePort).collect(Collectors.toList());
        if (portsWithNodePort.size() == 1) {
          return Optional.of(portsWithNodePort.get(0));
        }
      }
    }

    // If none of the above rules applied, we use the default port for backward compatibility.
    return defaultPort;
  }

  private boolean hasNodePort(Port port) {
    return port.getNodePort() != null && port.getNodePort() > 0;
  }

}
