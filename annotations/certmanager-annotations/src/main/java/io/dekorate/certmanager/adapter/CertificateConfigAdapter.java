/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.certmanager.adapter;

import java.util.Map;
import java.util.Optional;

import io.dekorate.certmanager.annotation.PrivateKeyAlgorithm;
import io.dekorate.certmanager.annotation.PrivateKeyEncoding;
import io.dekorate.certmanager.annotation.RotationPolicy;
import io.dekorate.certmanager.config.CA;
import io.dekorate.certmanager.config.CABuilder;
import io.dekorate.certmanager.config.CertificateConfig;
import io.dekorate.certmanager.config.CertificateConfigBuilder;
import io.dekorate.certmanager.config.CertificateKeystore;
import io.dekorate.certmanager.config.CertificateKeystoreBuilder;
import io.dekorate.certmanager.config.CertificateKeystores;
import io.dekorate.certmanager.config.CertificateKeystoresBuilder;
import io.dekorate.certmanager.config.CertificatePrivateKey;
import io.dekorate.certmanager.config.CertificatePrivateKeyBuilder;
import io.dekorate.certmanager.config.IssuerRef;
import io.dekorate.certmanager.config.IssuerRefBuilder;
import io.dekorate.certmanager.config.LocalObjectReference;
import io.dekorate.certmanager.config.LocalObjectReferenceBuilder;
import io.dekorate.certmanager.config.SelfSigned;
import io.dekorate.certmanager.config.SelfSignedBuilder;
import io.dekorate.certmanager.config.Subject;
import io.dekorate.certmanager.config.SubjectBuilder;
import io.dekorate.certmanager.config.Vault;
import io.dekorate.certmanager.config.VaultAppRole;
import io.dekorate.certmanager.config.VaultAppRoleBuilder;
import io.dekorate.certmanager.config.VaultBuilder;
import io.dekorate.certmanager.config.VaultKubernetesAuth;
import io.dekorate.certmanager.config.VaultKubernetesAuthBuilder;
import io.dekorate.utils.Strings;

public class CertificateConfigAdapter {

  public static CertificateConfigBuilder newBuilder(Map<String, Object> map) {
    return new CertificateConfigBuilder(getCertificateConfig(map));
  }

  private static CertificateConfig getCertificateConfig(Map<String, Object> map) {
    CertificateConfigBuilder certificate = new CertificateConfigBuilder()
        .withName(getOptionalString(map, "name").orElse(null))
        .withSecretName(getOptionalString(map, "secretName").orElse(null));

    // optional configuration
    getOptionalMap(map, "subject").ifPresent(s -> certificate.withSubject(getSubject(s)));
    getOptionalString(map, "commonName").ifPresent(certificate::withCommonName);
    getOptionalString(map, "duration").ifPresent(certificate::withDuration);
    getOptionalString(map, "renewBefore").ifPresent(certificate::withRenewBefore);
    getOptionalString(map, "volumeMountPath").ifPresent(certificate::withVolumeMountPath);
    getOptionalArrayString(map, "dnsNames").ifPresent(certificate::withDnsNames);
    getOptionalArrayString(map, "ipAddresses").ifPresent(certificate::withIpAddresses);
    getOptionalArrayString(map, "uris").ifPresent(certificate::withUris);
    getOptionalArrayString(map, "emailAddresses").ifPresent(certificate::withEmailAddresses);
    getOptionalArrayString(map, "usages").ifPresent(certificate::withUsages);
    getOptionalMap(map, "keystores").ifPresent(k -> certificate.withKeystores(getCertificateKeystores(k)));
    certificate.withIsCA(getBoolean(map, "isCA", false));
    getOptionalMap(map, "privateKey").ifPresent(p -> certificate.withPrivateKey(getCertificatePrivateKey(p)));
    certificate.withEncodeUsagesInRequest(getBoolean(map, "encodeUsagesInRequest", false));

    // issuers
    getOptionalMap(map, "issuerRef").ifPresent(i -> certificate.withIssuerRef(getIssuerRef(i)));
    getOptionalMap(map, "CA").ifPresent(i -> certificate.withCa(getCaConfig(i)));
    getOptionalMap(map, "vault").ifPresent(i -> certificate.withVault(getVaultConfig(i)));
    getOptionalMap(map, "selfSigned").filter(m -> isEnabled(m))
        .ifPresent(i -> certificate.withSelfSigned(getSelfSignedConfig(i)));

    return certificate.build();
  }

  private static SelfSigned getSelfSignedConfig(Map<String, Object> map) {
    SelfSignedBuilder builder = new SelfSignedBuilder();
    getOptionalArrayString(map, "crlDistributionPoints").ifPresent(builder::withCrlDistributionPoints);
    return builder.build();
  }

  private static Vault getVaultConfig(Map<String, Object> map) {
    Optional<String> server = getOptionalString(map, "server");
    Optional<String> path = getOptionalString(map, "path");
    if (!server.isPresent() && !path.isPresent()) {
      return null;
    }

    VaultBuilder issuer = new VaultBuilder()
        .withServer(server.get())
        .withPath(path.get());

    // optional configuration
    getOptionalMap(map, "authTokenSecretRef").ifPresent(a -> issuer.withAuthTokenSecretRef(getLocalObjectRef(a)));
    getOptionalMap(map, "authAppRole").ifPresent(a -> issuer.withAuthAppRole(getVaultAppRole(a)));
    getOptionalMap(map, "authKubernetes").ifPresent(a -> issuer.withAuthKubernetes(getVaultAuthKubernetes(a)));
    getOptionalString(map, "namespace").ifPresent(issuer::withNamespace);
    getOptionalString(map, "caBundle").ifPresent(issuer::withCaBundle);

    return issuer.build();
  }

  private static VaultKubernetesAuth getVaultAuthKubernetes(Map<String, Object> map) {
    VaultKubernetesAuthBuilder builder = new VaultKubernetesAuthBuilder();
    getOptionalString(map, "mountPath").ifPresent(builder::withMountPath);
    getOptionalString(map, "role").ifPresent(builder::withRole);
    getOptionalMap(map, "secretRef").ifPresent(s -> builder.withSecretRef(getLocalObjectRef(s)));

    return builder.build();
  }

  private static VaultAppRole getVaultAppRole(Map<String, Object> map) {
    VaultAppRoleBuilder builder = new VaultAppRoleBuilder();
    getOptionalString(map, "path").ifPresent(builder::withPath);
    getOptionalString(map, "roleId").ifPresent(builder::withRoleId);
    getOptionalMap(map, "secretRef").ifPresent(s -> builder.withSecretRef(getLocalObjectRef(s)));

    return builder.build();
  }

  private static CA getCaConfig(Map<String, Object> map) {
    Optional<String> secretName = getOptionalString(map, "secretName");
    if (!secretName.isPresent()) {
      return null;
    }

    CABuilder issuer = new CABuilder()
        .withSecretName(secretName.get());

    // optional configuration
    getOptionalArrayString(map, "crlDistributionPoints").ifPresent(issuer::withCrlDistributionPoints);

    return issuer.build();
  }

  private static CertificatePrivateKey getCertificatePrivateKey(Map<String, Object> map) {
    CertificatePrivateKeyBuilder builder = new CertificatePrivateKeyBuilder();
    getOptionalString(map, "rotationPolicy").map(RotationPolicy::valueOf).ifPresent(builder::withRotationPolicy);
    getOptionalString(map, "encoding").map(PrivateKeyEncoding::valueOf).ifPresent(builder::withEncoding);
    getOptionalString(map, "algorithm").map(PrivateKeyAlgorithm::valueOf).ifPresent(builder::withAlgorithm);
    getOptionalInteger(map, "size").ifPresent(builder::withSize);
    return builder.build();
  }

  private static CertificateKeystores getCertificateKeystores(Map<String, Object> map) {
    CertificateKeystoresBuilder builder = new CertificateKeystoresBuilder();
    getOptionalMap(map, "jks").ifPresent(c -> builder.withJks(getCertificateKeystore(c)));
    getOptionalMap(map, "pkcs12").ifPresent(c -> builder.withPkcs12(getCertificateKeystore(c)));
    return builder.build();
  }

  private static CertificateKeystore getCertificateKeystore(Map<String, Object> map) {
    CertificateKeystoreBuilder builder = new CertificateKeystoreBuilder()
        .withCreate(getBoolean(map, "create", false));
    getOptionalMap(map, "passwordSecretRef").ifPresent(p -> builder.withPasswordSecretRef(getLocalObjectRef(p)));
    return builder.build();
  }

  private static Subject getSubject(Map<String, Object> map) {
    SubjectBuilder builder = new SubjectBuilder();
    getOptionalArrayString(map, "organizations").ifPresent(builder::withOrganizations);
    getOptionalArrayString(map, "countries").ifPresent(builder::withCountries);
    getOptionalArrayString(map, "organizationalUnits").ifPresent(builder::withOrganizationalUnits);
    getOptionalArrayString(map, "localities").ifPresent(builder::withLocalities);
    getOptionalArrayString(map, "provinces").ifPresent(builder::withProvinces);
    getOptionalArrayString(map, "streetAddresses").ifPresent(builder::withStreetAddresses);
    getOptionalArrayString(map, "postalCodes").ifPresent(builder::withPostalCodes);
    getOptionalString(map, "serialNumber").ifPresent(builder::withSerialNumber);

    return builder.build();
  }

  private static IssuerRef getIssuerRef(Map<String, Object> objectMap) {
    Optional<String> name = getOptionalString(objectMap, "name");
    if (!name.isPresent()) {
      return null;
    }

    IssuerRefBuilder builder = new IssuerRefBuilder();
    builder.withName(name.get());
    getOptionalString(objectMap, "kind").ifPresent(builder::withKind);
    getOptionalString(objectMap, "group").ifPresent(builder::withGroup);
    return builder.build();
  }

  private static LocalObjectReference getLocalObjectRef(Map<String, Object> map) {
    LocalObjectReferenceBuilder builder = new LocalObjectReferenceBuilder();
    getOptionalString(map, "key").ifPresent(builder::withKey);
    getOptionalString(map, "name").ifPresent(builder::withName);
    return builder.build();
  }

  private static Optional<Integer> getOptionalInteger(Map<String, Object> map, String key) {
    return Optional.ofNullable(get(map, key)).map(o -> (Integer) o);
  }

  private static boolean isEnabled(Map<String, Object> map) {
    return getBoolean(map, "enabled", false);
  }

  private static boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
    return Optional.ofNullable(get(map, key)).map(v -> {
      if (v instanceof Boolean) {
        return (Boolean) v;
      } else if (v instanceof String && Strings.isNotNullOrEmpty((String) v)) {
        return Boolean.parseBoolean((String) v);
      }

      return defaultValue;
    }).orElse(defaultValue);
  }

  private static Optional<String> getOptionalString(Map<String, Object> map, String key) {
    return Optional.ofNullable(get(map, key)).map(o -> (String) o).filter(Strings::isNotNullOrEmpty);
  }

  private static Optional<Map<String, Object>> getOptionalMap(Map<String, Object> map, String key) {
    return Optional.ofNullable(get(map, key)).map(o -> (Map<String, Object>) o);
  }

  private static Optional<String[]> getOptionalArrayString(Map<String, Object> map, String key) {
    return Optional.ofNullable(get(map, key)).map(o -> (String[]) o).filter(a -> a.length > 0);
  }

  private static Object get(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value == null && key.contains("-")) {
      // try with camel case
      value = map.get(Strings.kebabToCamelCase(key));
    }

    if (value == null) {
      // try with kebab case
      value = map.get(Strings.camelToKebabCase(key));
    }

    return value;
  }
}
