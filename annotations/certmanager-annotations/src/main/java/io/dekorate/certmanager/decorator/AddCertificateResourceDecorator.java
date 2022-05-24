package io.dekorate.certmanager.decorator;

import java.text.ParseException;
import java.util.Optional;

import io.dekorate.certmanager.annotation.PrivateKeyAlgorithm;
import io.dekorate.certmanager.annotation.PrivateKeyEncoding;
import io.dekorate.certmanager.annotation.RotationPolicy;
import io.dekorate.certmanager.config.CertificateConfig;
import io.dekorate.certmanager.config.CertificateKeystores;
import io.dekorate.certmanager.config.CertificatePrivateKey;
import io.dekorate.certmanager.config.IssuerRef;
import io.dekorate.certmanager.config.LocalObjectReference;
import io.dekorate.certmanager.config.Subject;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.utils.Strings;
import io.fabric8.certmanager.api.model.meta.v1.ObjectReference;
import io.fabric8.certmanager.api.model.meta.v1.ObjectReferenceBuilder;
import io.fabric8.certmanager.api.model.meta.v1.SecretKeySelector;
import io.fabric8.certmanager.api.model.meta.v1.SecretKeySelectorBuilder;
import io.fabric8.certmanager.api.model.v1.CertificateBuilder;
import io.fabric8.certmanager.api.model.v1.CertificateFluent;
import io.fabric8.certmanager.api.model.v1.CertificateKeystoresBuilder;
import io.fabric8.certmanager.api.model.v1.CertificatePrivateKeyBuilder;
import io.fabric8.certmanager.api.model.v1.X509Subject;
import io.fabric8.certmanager.api.model.v1.X509SubjectBuilder;
import io.fabric8.kubernetes.api.model.Duration;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

public class AddCertificateResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private final CertificateConfig config;

  public AddCertificateResourceDecorator(CertificateConfig config) {
    this.config = config;
  }

  @Override
  public void visit(KubernetesListBuilder list) {
    HasMetadata meta = getMandatoryDeploymentHasMetadata(list, ANY);
    CertificateBuilder builder = new CertificateBuilder();

    // metadata
    builder.withNewMetadata()
        .withName(getName(meta))
        .withNamespace(meta.getMetadata().getNamespace())
        .endMetadata();

    // mandatory configuration
    CertificateFluent.SpecNested<CertificateBuilder> spec = builder.withNewSpec().withSecretName(config.getSecretName());

    // issuer ref: it can be set or be auto generated (when it's auto generated, the name is the same as the Certificate res
    if (config.getIssuerRef() != null) {
      spec.withIssuerRef(toIssuerRef(config.getIssuerRef()));
    } else {
      spec.withNewIssuerRef().withName(getName(meta)).endIssuerRef();
    }

    // optional configuration
    Optional.ofNullable(config.getSubject()).map(this::toSubject).ifPresent(spec::withSubject);
    Optional.ofNullable(config.getCommonName()).ifPresent(spec::withCommonName);
    Optional.ofNullable(config.getDuration()).map(this::toDuration).ifPresent(spec::withDuration);
    Optional.ofNullable(config.getRenewBefore()).map(this::toDuration).ifPresent(spec::withRenewBefore);
    Optional.ofNullable(config.getDnsNames()).ifPresent(spec::withDnsNames);
    Optional.ofNullable(config.getIpAddresses()).ifPresent(spec::withIpAddresses);
    Optional.ofNullable(config.getUris()).ifPresent(spec::withUris);
    Optional.ofNullable(config.getEmailAddresses()).ifPresent(spec::withEmailAddresses);
    Optional.ofNullable(config.getUsages()).ifPresent(spec::withUsages);
    Optional.ofNullable(config.getKeystores()).map(this::toCertificateKeystores).ifPresent(spec::withKeystores);
    Optional.ofNullable(config.getIsCA()).ifPresent(spec::withIsCA);
    Optional.ofNullable(config.getPrivateKey()).map(this::toPrivateKey).ifPresent(spec::withPrivateKey);
    Optional.ofNullable(config.getEncodeUsagesInRequest()).ifPresent(spec::withEncodeUsagesInRequest);

    list.addToItems(spec.endSpec().build());
  }

  private io.fabric8.certmanager.api.model.v1.CertificatePrivateKey toPrivateKey(CertificatePrivateKey privateKey) {
    CertificatePrivateKeyBuilder builder = new CertificatePrivateKeyBuilder();
    if (privateKey.getRotationPolicy() != null && privateKey.getRotationPolicy() != RotationPolicy.Unset) {
      builder.withRotationPolicy(privateKey.getRotationPolicy().name());
    }

    if (privateKey.getAlgorithm() != null && privateKey.getAlgorithm() != PrivateKeyAlgorithm.Unset) {
      builder.withAlgorithm(privateKey.getAlgorithm().name());
    }

    if (privateKey.getEncoding() != null && privateKey.getEncoding() != PrivateKeyEncoding.Unset) {
      builder.withEncoding(privateKey.getEncoding().name());
    }

    if (privateKey.getSize() >= 0) {
      builder.withSize(privateKey.getSize());
    }

    return builder.build();
  }

  private io.fabric8.certmanager.api.model.v1.CertificateKeystores toCertificateKeystores(CertificateKeystores keystores) {
    CertificateKeystoresBuilder builder = new CertificateKeystoresBuilder();
    if (keystores.getJks() != null) {
      builder.withNewJks().withCreate(keystores.getJks().getCreate())
          .withPasswordSecretRef(toSecretKeySelector(keystores.getJks().getPasswordSecretRef()))
          .endJks();
    }

    if (keystores.getPkcs12() != null) {
      builder.withNewPkcs12().withCreate(keystores.getPkcs12().getCreate())
          .withPasswordSecretRef(toSecretKeySelector(keystores.getPkcs12().getPasswordSecretRef()))
          .endPkcs12();
    }

    return builder.build();
  }

  private SecretKeySelector toSecretKeySelector(LocalObjectReference ref) {
    if (ref == null) {
      return null;
    }

    SecretKeySelectorBuilder builder = new SecretKeySelectorBuilder();
    builder.withName(ref.getName());
    builder.withKey(ref.getKey());
    return builder.build();
  }

  private Duration toDuration(String str) {
    try {
      return Duration.parse(str);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Wrong duration format '" + str + "'", e);
    }
  }

  private ObjectReference toIssuerRef(IssuerRef issuerRef) {
    ObjectReferenceBuilder builder = new ObjectReferenceBuilder();
    builder.withName(issuerRef.getName());
    if (Strings.isNotNullOrEmpty(issuerRef.getKind())) {
      builder.withKind(issuerRef.getKind());
    }

    if (Strings.isNotNullOrEmpty(issuerRef.getGroup())) {
      builder.withGroup(issuerRef.getGroup());
    }

    return builder.build();
  }

  private X509Subject toSubject(Subject s) {
    X509SubjectBuilder builder = new X509SubjectBuilder();
    Optional.ofNullable(s.getCountries()).ifPresent(builder::withCountries);
    Optional.ofNullable(s.getLocalities()).ifPresent(builder::withLocalities);
    Optional.ofNullable(s.getOrganizationalUnits()).ifPresent(builder::withOrganizationalUnits);
    Optional.ofNullable(s.getOrganizations()).ifPresent(builder::withOrganizations);
    Optional.ofNullable(s.getProvinces()).ifPresent(builder::withProvinces);
    Optional.ofNullable(s.getPostalCodes()).ifPresent(builder::withPostalCodes);
    Optional.ofNullable(s.getSerialNumber()).ifPresent(builder::withSerialNumber);
    Optional.ofNullable(s.getStreetAddresses()).ifPresent(builder::withStreetAddresses);
    return builder.build();
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class };
  }

  private String getName(HasMetadata meta) {
    if (Strings.isNullOrEmpty(config.getName())) {
      return meta.getMetadata().getName();
    }

    return config.getName();
  }

}
