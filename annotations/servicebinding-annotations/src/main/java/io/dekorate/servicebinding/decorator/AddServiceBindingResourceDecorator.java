package io.dekorate.servicebinding.decorator;

import java.util.Arrays;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.servicebinding.config.ApplicationConfig;
import io.dekorate.servicebinding.config.BindingPathConfig;
import io.dekorate.servicebinding.config.ServiceBindingConfig;
import io.dekorate.servicebinding.config.ServiceConfig;
import io.dekorate.servicebinding.model.Application;
import io.dekorate.servicebinding.model.BindingPath;
import io.dekorate.servicebinding.model.ConfigMapKeyRef;
import io.dekorate.servicebinding.model.ConfigMapKeyRefBuilder;
import io.dekorate.servicebinding.model.CustomEnvVar;
import io.dekorate.servicebinding.model.CustomEnvVarBuilder;
import io.dekorate.servicebinding.model.FieldRef;
import io.dekorate.servicebinding.model.FieldRefBuilder;
import io.dekorate.servicebinding.model.ResourceFieldRef;
import io.dekorate.servicebinding.model.ResourceFieldRefBuilder;
import io.dekorate.servicebinding.model.SecretKeyRef;
import io.dekorate.servicebinding.model.SecretKeyRefBuilder;
import io.dekorate.servicebinding.model.Service;
import io.dekorate.servicebinding.model.ServiceBindingBuilder;
import io.dekorate.servicebinding.model.ValueFrom;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

public class AddServiceBindingResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private final ServiceBindingConfig config;

  private final Logger LOGGER = LoggerFactory.getLogger();

  public AddServiceBindingResourceDecorator(ServiceBindingConfig config) {
    this.config = config;
  }

  @Override
  public void visit(KubernetesListBuilder list) {
    HasMetadata meta = getMandatoryDeploymentHasMetadata(list);
    ServiceBindingBuilder serviceBindingBuilder = new ServiceBindingBuilder().withNewMetadata()
        .withName(getServiceBindingName(config.getName(), meta.getMetadata().getName()))
        .withNamespace(meta.getMetadata().getNamespace()).endMetadata().withNewSpec()
        .withApplication(getApplication(config.getApplication(), config.getBindingPath(), meta))
        .withEnvVarPrefix(getEnvVarPrefix(config.getEnvVarPrefix())).withServices(getServices(config.getServices()))
        .withCustomEnvVar(getCustomEnvVar(config.getCustomEnvVar()))
        .withDetectBindingResources(config.isDetectBindingResources()).withBindAsFiles(config.isBindAsFiles())
        .withMountPath(!config.getMountPath().equals("") ? config.getMountPath() : null).endSpec();
    list.addToItems(serviceBindingBuilder);
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class };
  }

  private Service[] getServices(ServiceConfig[] services) {
    return Arrays.stream(services)
        .map(s -> new Service(s.getGroup(), s.getKind(), s.getName(), s.getVersion(), s.getId(),
            Strings.isNotNullOrEmpty(s.getNamespace()) ? s.getNamespace() : null,
            Strings.isNotNullOrEmpty(s.getEnvVarPrefix()) ? s.getEnvVarPrefix() : null))
        .toArray(Service[]::new);
  }

  private Application getApplication(ApplicationConfig config, BindingPathConfig bindingPathConfig, HasMetadata meta) {
    String[] apiVersion = meta.getApiVersion().split("/");
    String name = config != null && Strings.isNotNullOrEmpty(config.getName()) ? config.getName()
        : meta.getMetadata().getName();
    return new Application(apiVersion[0], meta.getKind(), name, apiVersion[1], getBindingPath(bindingPathConfig));
  }

  private String getServiceBindingName(String serviceBindingName, String deploymentName) {
    if (Strings.isNotNullOrEmpty(serviceBindingName)) {
      return serviceBindingName;
    }
    return deploymentName + "-binding";
  }

  private BindingPath getBindingPath(BindingPathConfig config) {
    if (config == null) {
      return null;
    }
    String containerPath = Strings.isNotNullOrEmpty(config.getContainerPath()) ? config.getContainerPath() : null;
    String secretPath = Strings.isNotNullOrEmpty(config.getSecretPath()) ? config.getSecretPath() : null;
    if (containerPath == null && secretPath == null) {
      return null;
    }
    return new BindingPath(containerPath, secretPath);
  }

  private CustomEnvVar[] getCustomEnvVar(Env[] config) {
    return Arrays.stream(config)
        .map(s -> new CustomEnvVarBuilder().withName(Strings.isNotNullOrEmpty(s.getName()) ? s.getName() : null)
            .withValue(getEnvValue(s)).withValueFrom(getValueFrom(s)).build())
        .toArray(CustomEnvVar[]::new);
  }

  private String getEnvValue(Env envConfig) {
    if (Strings.isNotNullOrEmpty(envConfig.getConfigmap()) || Strings.isNotNullOrEmpty(envConfig.getSecret())
        || Strings.isNotNullOrEmpty(envConfig.getField())) {
      return null;
    } else {
      return !envConfig.getValue().equals("") ? envConfig.getValue() : null;
    }
  }

  private String getEnvVarPrefix(String envVarPrefix) {
    return Strings.isNotNullOrEmpty(envVarPrefix) ? envVarPrefix : null;
  }

  private ValueFrom getValueFrom(Env envConfig) {
    if (envConfig == null) {
      return null;
    }
    ConfigMapKeyRef configMapKeyRef = null;
    SecretKeyRef secretKeyRef = null;
    FieldRef fieldRef = null;
    ResourceFieldRef resourceFieldRef = null;

    if (Strings.isNotNullOrEmpty(envConfig.getConfigmap())) {
      configMapKeyRef = getConfigMapKeyRef(envConfig);
    }

    if (Strings.isNotNullOrEmpty(envConfig.getSecret())) {
      secretKeyRef = getSecretKeyRef(envConfig);
    }

    if (Strings.isNotNullOrEmpty(envConfig.getField())) {
      fieldRef = getFieldRef(envConfig);
    }

    if (Strings.isNotNullOrEmpty(envConfig.getResourceField())) {
      resourceFieldRef = getResourceFieldRef(envConfig);
    }

    if (configMapKeyRef == null && secretKeyRef == null && fieldRef == null && resourceFieldRef == null) {
      return null;
    }
    return new ValueFrom(configMapKeyRef, secretKeyRef, fieldRef, resourceFieldRef);
  }

  private ConfigMapKeyRef getConfigMapKeyRef(Env envConfig) {
    if (Strings.isNullOrEmpty(envConfig.getConfigmap())) {
      return null;
    }
    return new ConfigMapKeyRefBuilder().withKey(envConfig.getValue()).withName(envConfig.getConfigmap()).build();
  }

  private SecretKeyRef getSecretKeyRef(Env envConfig) {
    if (Strings.isNullOrEmpty(envConfig.getSecret())) {
      return null;
    }
    return new SecretKeyRefBuilder().withKey(envConfig.getValue()).withName(envConfig.getSecret()).build();
  }

  private FieldRef getFieldRef(Env envConfig) {
    if (Strings.isNullOrEmpty(envConfig.getField())) {
      return null;
    }
    return new FieldRefBuilder().withFieldPath(envConfig.getField()).build();
  }

  private ResourceFieldRef getResourceFieldRef(Env envConfig) {
    LOGGER.info("------->>>>");
    LOGGER.info(envConfig.getResourceField());
    if (Strings.isNullOrEmpty(envConfig.getResourceField())) {
      return null;
    }
    return new ResourceFieldRefBuilder().withResource(envConfig.getResourceField()).build();
  }

}
