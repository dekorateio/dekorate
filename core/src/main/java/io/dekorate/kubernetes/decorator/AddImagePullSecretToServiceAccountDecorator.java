
package io.dekorate.kubernetes.decorator;

import java.util.List;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ServiceAccountFluent;

public class AddImagePullSecretToServiceAccountDecorator extends NamedResourceDecorator<ServiceAccountFluent<?>> {

  private final List<String> imagePullSecrets;

  public AddImagePullSecretToServiceAccountDecorator(String name, List<String> imagePullSecrets) {
    super(name);
    this.imagePullSecrets = imagePullSecrets;
  }

  @Override
  public void andThenVisit(ServiceAccountFluent<?> serviceAccount, ObjectMeta meta) {
    serviceAccount
        .addAllToImagePullSecrets(imagePullSecrets.stream().map(s -> new LocalObjectReference(s)).collect(Collectors.toList()));
  }
}
