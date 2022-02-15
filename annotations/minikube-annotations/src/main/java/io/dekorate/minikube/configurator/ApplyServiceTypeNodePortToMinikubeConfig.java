package io.dekorate.minikube.configurator;

import io.dekorate.kubernetes.annotation.ServiceType;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.minikube.config.MinikubeConfigFluent;

public class ApplyServiceTypeNodePortToMinikubeConfig extends Configurator<MinikubeConfigFluent> {

  @Override
  public void visit(MinikubeConfigFluent config) {
    if (!config.hasServiceType()) {
      config.withServiceType(ServiceType.NodePort);
    }
  }
}
