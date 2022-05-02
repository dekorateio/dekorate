package io.dekorate.certmanager.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dekorate.Session;
import io.dekorate.SessionWriter;
import io.dekorate.WithProject;
import io.dekorate.processor.SimpleFileWriter;
import io.dekorate.project.FileProjectFactory;
import io.fabric8.certmanager.api.model.v1.Certificate;
import io.fabric8.certmanager.api.model.v1.Issuer;
import io.fabric8.kubernetes.api.model.Duration;
import io.fabric8.kubernetes.api.model.KubernetesList;

public class CertificateConfigGeneratorTest {

  private Session session;
  private HashMap<String, Object> certificate;

  @BeforeEach
  public void setup() throws IOException {
    Session.clearSession();
    Path tempDir = Files.createTempDirectory("dekorate");

    WithProject withProject = new WithProject() {
    };
    withProject
        .setProject(FileProjectFactory.create(new File(".")).withDekorateOutputDir(tempDir.toAbsolutePath().toString())
            .withDekorateMetaDir(tempDir.toAbsolutePath().toString()));

    SessionWriter writer = new SimpleFileWriter(withProject.getProject(), false);
    session = Session.getSession();
    session.setWriter(writer);

    certificate = new HashMap<String, Object>();
    certificate.put("name", "my-certificate");
    certificate.put("secretName", "my-secret-name");
  }

  @Test
  public void shouldGenerateCertificateWithIssuerRef() {
    final HashMap<String, Object> issuerRef = new HashMap<String, Object>() {
      {
        put("name", "my-issuer");
      }
    };

    final HashMap<String, Object> subject = new HashMap<String, Object>() {
      {
        put("organizations", new String[] { "org1", "org2" });
        put("countries", new String[] { "country1", "country2" });
        put("organizationalUnits", new String[] { "unit1" });
        put("localities", new String[] { "local1", "local2" });
        put("provinces", new String[] { "prov1", "prov2" });
        put("streetAddresses", new String[] { "str1", "str2" });
        put("postalCodes", new String[] { "post1", "post2" });
        put("serialNumber", "serial");
      }
    };

    final HashMap<String, Object> localObjectReference = new HashMap<String, Object>() {
      {
        put("name", "a name");
        put("key", "a key");
      }
    };

    final HashMap<String, Object> keystore = new HashMap<String, Object>() {
      {
        put("create", "true");
        put("passwordSecretRef", localObjectReference);
      }
    };

    final HashMap<String, Object> keystores = new HashMap<String, Object>() {
      {
        put("jks", keystore);
        put("pkcs12", keystore);
      }
    };

    final HashMap<String, Object> privateKey = new HashMap<String, Object>() {
      {
        put("rotationPolicy", "Never");
        put("encoding", "PKCS1");
        put("algorithm", "RSA");
        put("size", 1050);
      }
    };

    certificate.put("issuerRef", issuerRef);
    certificate.put("subject", subject);
    certificate.put("common-name", "my-commonName");
    certificate.put("duration", "24h");
    certificate.put("renewBefore", "24h");
    certificate.put("dnsNames", new String[] { "dns1", "dns2" });
    certificate.put("ipAddresses", new String[] { "ip1", "ip2" });
    certificate.put("uris", new String[] { "uri1", "uri2" });
    certificate.put("emailAddresses", new String[] { "a@b.com" });
    certificate.put("keystores", keystores);
    certificate.put("isCA", "true");
    certificate.put("usages", new String[] { "us1", "us2" });
    certificate.put("privateKey", privateKey);
    certificate.put("encode-usages-in-request", "true");

    KubernetesList list = generate();
    assertThat(list).isNotNull();
    assertThat(list.getItems()).filteredOn(i -> "Certificate".equals(i.getKind()))
        .hasOnlyOneElementSatisfying(item -> {
          assertThat(item).isInstanceOfSatisfying(Certificate.class, s -> {
            assertThat(s.getSpec()).satisfies(spec -> {
              assertThat(spec.getSecretName()).isEqualTo("my-secret-name");
              assertThat(spec.getCommonName()).isEqualTo("my-commonName");
              assertThat(spec.getIssuerRef().getName()).isEqualTo("my-issuer");
              assertThat(spec.getDuration()).isEqualTo(durationOf("24h"));
              assertThat(spec.getRenewBefore()).isEqualTo(durationOf("24h"));
              assertThat(spec.getDnsNames()).containsExactly("dns1", "dns2");
              assertThat(spec.getIpAddresses()).containsExactly("ip1", "ip2");
              assertThat(spec.getUris()).containsExactly("uri1", "uri2");
              assertThat(spec.getEmailAddresses()).containsExactly("a@b.com");
              assertThat(spec.getIsCA()).isTrue();
              assertThat(spec.getUsages()).containsExactly("us1", "us2");
              assertThat(spec.getEncodeUsagesInRequest()).isTrue();

              // subject
              assertThat(spec.getSubject()).isNotNull();
              assertThat(spec.getSubject().getOrganizations()).containsExactly("org1", "org2");
              assertThat(spec.getSubject().getCountries()).containsExactly("country1", "country2");
              assertThat(spec.getSubject().getOrganizationalUnits()).containsExactly("unit1");
              assertThat(spec.getSubject().getSerialNumber()).isEqualTo("serial");
              assertThat(spec.getSubject().getProvinces()).containsExactly("prov1", "prov2");
              assertThat(spec.getSubject().getStreetAddresses()).containsExactly("str1", "str2");
              assertThat(spec.getSubject().getPostalCodes()).containsExactly("post1", "post2");

              // keystores
              assertThat(spec.getKeystores()).isNotNull();
              assertThat(spec.getKeystores().getJks()).isNotNull();
              assertThat(spec.getKeystores().getJks().getCreate()).isTrue();
              assertThat(spec.getKeystores().getJks().getPasswordSecretRef()).isNotNull();
              assertThat(spec.getKeystores().getJks().getPasswordSecretRef().getName()).isEqualTo("a name");
              assertThat(spec.getKeystores().getJks().getPasswordSecretRef().getKey()).isEqualTo("a key");
              assertThat(spec.getKeystores().getPkcs12()).isNotNull();
              assertThat(spec.getKeystores().getPkcs12().getCreate()).isTrue();
              assertThat(spec.getKeystores().getPkcs12().getPasswordSecretRef()).isNotNull();
              assertThat(spec.getKeystores().getPkcs12().getPasswordSecretRef().getName()).isEqualTo("a name");
              assertThat(spec.getKeystores().getPkcs12().getPasswordSecretRef().getKey()).isEqualTo("a key");

              // privateKey
              assertThat(spec.getPrivateKey()).isNotNull();
              assertThat(spec.getPrivateKey().getRotationPolicy()).isEqualTo("Never");
              assertThat(spec.getPrivateKey().getAlgorithm()).isEqualTo("RSA");
              assertThat(spec.getPrivateKey().getEncoding()).isEqualTo("PKCS1");
              assertThat(spec.getPrivateKey().getSize()).isEqualTo(1050);
            });
          });
        });
  }

  @Test
  public void shouldGenerateCertificateWithIssuerOfTypeCA() {
    final HashMap<String, Object> issuer = new HashMap<String, Object>();
    issuer.put("secretName", "my-secret-name");
    issuer.put("crlDistributionPoints", new String[] { "crl1", "crl2" });

    certificate.put("CA", issuer);

    KubernetesList list = generate();
    assertThat(list).isNotNull();
    assertThat(list.getItems()).filteredOn(i -> "Issuer".equals(i.getKind()))
        .hasOnlyOneElementSatisfying(item -> {
          assertThat(item).isInstanceOfSatisfying(Issuer.class, s -> {
            assertThat(s.getSpec()).satisfies(spec -> {
              assertThat(spec.getCa()).isNotNull();
              assertThat(spec.getAcme()).isNull();
              assertThat(spec.getVault()).isNull();
              assertThat(spec.getSelfSigned()).isNull();
              assertThat(spec.getCa().getSecretName()).isEqualTo("my-secret-name");
              assertThat(spec.getCa().getCrlDistributionPoints()).containsExactly("crl1", "crl2");
            });
          });
        });
    assertThat(list.getItems()).filteredOn(i -> "Certificate".equals(i.getKind()))
        .hasOnlyOneElementSatisfying(item -> {
          assertThat(item).isInstanceOfSatisfying(Certificate.class, s -> {
            assertThat(s.getSpec()).satisfies(spec -> {
              assertThat(spec.getIssuerRef().getName()).isEqualTo(item.getMetadata().getName());
            });
          });
        });
  }

  @Test
  public void shouldGenerateCertificateWithIssuerOfTypeVault() {
    final HashMap<String, Object> localObjectReference = new HashMap<String, Object>() {
      {
        put("name", "a name");
        put("key", "a key");
      }
    };

    final HashMap<String, Object> issuer = new HashMap<String, Object>();
    issuer.put("server", "my-server");
    issuer.put("path", "my-path");
    issuer.put("namespace", "my-namespace");
    issuer.put("authTokenSecretRef", localObjectReference);

    certificate.put("vault", issuer);

    KubernetesList list = generate();
    assertThat(list).isNotNull();
    assertThat(list.getItems()).filteredOn(i -> "Issuer".equals(i.getKind()))
        .hasOnlyOneElementSatisfying(item -> {
          assertThat(item).isInstanceOfSatisfying(Issuer.class, s -> {
            assertThat(s.getSpec()).satisfies(spec -> {
              assertThat(spec.getVault()).isNotNull();
              assertThat(spec.getAcme()).isNull();
              assertThat(spec.getCa()).isNull();
              assertThat(spec.getSelfSigned()).isNull();
              assertThat(spec.getVault().getPath()).isEqualTo("my-path");
              assertThat(spec.getVault().getServer()).isEqualTo("my-server");
              assertThat(spec.getVault().getNamespace()).isEqualTo("my-namespace");
              assertThat(spec.getVault().getAuth().getTokenSecretRef()).isNotNull();
              assertThat(spec.getVault().getAuth().getTokenSecretRef().getKey()).isEqualTo("a key");
              assertThat(spec.getVault().getAuth().getTokenSecretRef().getName()).isEqualTo("a name");
            });
          });
        });
    assertThat(list.getItems()).filteredOn(i -> "Certificate".equals(i.getKind()))
        .hasOnlyOneElementSatisfying(item -> {
          assertThat(item).isInstanceOfSatisfying(Certificate.class, s -> {
            assertThat(s.getSpec()).satisfies(spec -> {
              assertThat(spec.getIssuerRef().getName()).isEqualTo(item.getMetadata().getName());
            });
          });
        });
  }

  @Test
  public void shouldGenerateCertificateWithIssuerOfTypeSelfSigned() {

    final HashMap<String, Object> issuer = new HashMap<String, Object>();
    issuer.put("enabled", "true");

    certificate.put("selfSigned", issuer);

    KubernetesList list = generate();
    assertThat(list).isNotNull();
    assertThat(list.getItems()).filteredOn(i -> "Issuer".equals(i.getKind()))
        .hasOnlyOneElementSatisfying(item -> {
          assertThat(item).isInstanceOfSatisfying(Issuer.class, s -> {
            assertThat(s.getSpec()).satisfies(spec -> {
              assertThat(spec.getVault()).isNull();
              assertThat(spec.getAcme()).isNull();
              assertThat(spec.getCa()).isNull();
              assertThat(spec.getSelfSigned()).isNotNull();
            });
          });
        });
    assertThat(list.getItems()).filteredOn(i -> "Certificate".equals(i.getKind()))
        .hasOnlyOneElementSatisfying(item -> {
          assertThat(item).isInstanceOfSatisfying(Certificate.class, s -> {
            assertThat(s.getSpec()).satisfies(spec -> {
              assertThat(spec.getIssuerRef().getName()).isEqualTo(item.getMetadata().getName());
            });
          });
        });
  }

  private KubernetesList generate() {
    Map<String, Object> map = new HashMap<String, Object>() {
      {
        put(CertificateConfig.class.getName(), certificate);
      }
    };

    CertificateConfigGenerator generator = new DefaultCertificateConfigGenerator(session.getConfigurationRegistry());
    generator.addPropertyConfiguration(map);

    session.close();
    return session.getGeneratedResources().get("kubernetes");
  }

  private static final Duration durationOf(String parse) {
    try {
      return Duration.parse(parse);
    } catch (ParseException e) {
      fail(e.getMessage());
    }

    return null;
  }
}
