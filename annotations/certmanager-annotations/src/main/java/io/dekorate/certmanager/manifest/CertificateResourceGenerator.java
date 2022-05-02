package io.dekorate.certmanager.manifest;

import java.util.Objects;
import java.util.stream.Stream;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.ManifestGenerator;
import io.dekorate.ResourceRegistry;
import io.dekorate.certmanager.config.CertificateConfig;
import io.dekorate.certmanager.config.EditableCertificateConfig;
import io.dekorate.certmanager.decorator.AddCaIssuerResourceDecorator;
import io.dekorate.certmanager.decorator.AddCertificateResourceDecorator;
import io.dekorate.certmanager.decorator.AddSelfSignedIssuerResourceDecorator;
import io.dekorate.certmanager.decorator.AddVaultIssuerResourceDecorator;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.MountBuilder;
import io.dekorate.kubernetes.config.SecretVolumeBuilder;
import io.dekorate.kubernetes.decorator.AddMountDecorator;
import io.dekorate.kubernetes.decorator.AddSecretVolumeDecorator;
import io.dekorate.utils.Strings;

public class CertificateResourceGenerator implements ManifestGenerator<CertificateConfig> {

  private static final String DEFAULT_VOLUME_NAME = "volume-certs";
  private static final String DEFAULT_VOLUME_MOUNT_PATH = "/etc/certs";

  private final ResourceRegistry resourceRegistry;
  private final ConfigurationRegistry configurationRegistry;

  private static final String CERTIFICATE = "certificate";

  private final Logger LOGGER = LoggerFactory.getLogger();

  public CertificateResourceGenerator(ResourceRegistry resources, ConfigurationRegistry configurationRegistry) {
    this.resourceRegistry = resources;
    this.configurationRegistry = configurationRegistry;
  }

  @Override
  public int order() {
    return 600;
  }

  @Override
  public String getKey() {
    return CERTIFICATE;
  }

  public boolean accepts(Class<? extends Configuration> type) {
    return type.equals(CertificateConfig.class) || type.equals(EditableCertificateConfig.class);
  }

  public void generate(CertificateConfig config) {
    LOGGER.info("Processing certificate config.");
    validate(config);

    // issuers
    if (config.getVault() != null) {
      resourceRegistry.decorate(new AddVaultIssuerResourceDecorator(config.getVault(), config.getName()));
    }

    if (config.getCa() != null) {
      resourceRegistry.decorate(new AddCaIssuerResourceDecorator(config.getCa(), config.getName()));
    }

    if (config.getSelfSigned() != null) {
      resourceRegistry.decorate(new AddSelfSignedIssuerResourceDecorator(config.getSelfSigned(), config.getName()));
    }

    // certificate
    resourceRegistry.decorate(new AddCertificateResourceDecorator(config));

    // volumes
    String volumeName = getStringOrDefault(config.getName(), DEFAULT_VOLUME_NAME);
    resourceRegistry.decorate(new AddMountDecorator(new MountBuilder()
        .withName(getStringOrDefault(config.getName(), DEFAULT_VOLUME_NAME))
        .withPath(getStringOrDefault(config.getVolumeMountPath(), DEFAULT_VOLUME_MOUNT_PATH))
        .withReadOnly(true)
        .build()));

    resourceRegistry.decorate(new AddSecretVolumeDecorator(new SecretVolumeBuilder()
        .withSecretName(config.getSecretName())
        .withVolumeName(volumeName)
        .build()));
  }

  public void validate(CertificateConfig config) {
    Object[] issuers = getAllIssuers(config);
    if (noneIssuerIsSet(issuers)) {
      throw new IllegalArgumentException("No issuer has been set in the certificate");
    }

    if (moreThanOneIssuerIsSet(issuers)) {
      throw new IllegalArgumentException("More then one issuer have been set in the certificate");
    }
  }

  private Object[] getAllIssuers(CertificateConfig config) {
    return new Object[] { config.getIssuerRef(), config.getCa(), config.getVault(), config.getSelfSigned() };
  }

  private boolean moreThanOneIssuerIsSet(Object... issuers) {
    return Stream.of(issuers).filter(Objects::nonNull).count() > 1;
  }

  private boolean noneIssuerIsSet(Object... issuers) {
    return Stream.of(issuers).noneMatch(Objects::nonNull);
  }

  private String getStringOrDefault(String value, String defaultStr) {
    return Strings.isNullOrEmpty(value) ? defaultStr : value;
  }
}
