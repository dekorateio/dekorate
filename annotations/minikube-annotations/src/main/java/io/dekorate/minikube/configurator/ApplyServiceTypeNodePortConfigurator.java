package io.dekorate.minikube.configurator;

import io.dekorate.kubernetes.annotation.ServiceType;
import io.dekorate.kubernetes.config.BaseConfigFluent;
import io.dekorate.kubernetes.config.Configurator;

public class ApplyServiceTypeNodePortConfigurator extends Configurator<BaseConfigFluent> {

  @Override
  public void visit(BaseConfigFluent config) {
    config.withServiceType(ServiceType.NodePort);
  }
}
