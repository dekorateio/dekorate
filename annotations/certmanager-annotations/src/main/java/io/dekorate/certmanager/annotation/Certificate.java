/**
 * Copyright 2018 The original authors.
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
 **/

package io.dekorate.certmanager.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.dekorate.kubernetes.config.Configuration;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
@Pojo(name = "CertificateConfig", autobox = true, mutable = true, superClass = Configuration.class, relativePath = "../config", withStaticBuilderMethod = true, withStaticAdapterMethod = false)
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface Certificate {

  /**
   * @return name of the certificate resource to be generated.
   */
  String name() default "";

  /**
   * SecretName is the name of the secret resource that will be automatically created and managed by this Certificate resource.
   * It will be populated with a private key and certificate, signed by the denoted issuer.
   * 
   * @return the name of the secret resource that will be automatically created and managed by this Certificate resource.
   */
  String secretName();

  /**
   * @return the reference to the issuer for this certificate.
   */
  IssuerRef issuerRef() default @IssuerRef;

  /**
   * @return the CA issuer configuration.
   */
  CA ca() default @CA;

  /**
   * @return the Vault issuer configuration.
   */
  Vault vault() default @Vault;

  /**
   * @return the self-signed issuer configuration.
   */
  SelfSigned selfSigned() default @SelfSigned;

  /**
   * Full X509 name specification (https://golang.org/pkg/crypto/x509/pkix/#Name).
   * 
   * @return the full X509 name specification
   */
  Subject subject() default @Subject;

  /**
   * CommonName is a common name to be used on the Certificate. The CommonName should have a length of 64 characters or fewer
   * to avoid generating invalid CSRs.
   * 
   * @return the common name.
   */
  String commonName() default "";

  /**
   * @return the lifetime of the Certificate.
   */
  String duration() default "";

  /**
   * How long before the currently issued certificate’s expiry cert-manager should renew the certificate.
   * The default is 2⁄3 of the issued certificate’s duration.
   *
   * @return the renewal before duration timestamp.
   */
  String renewBefore() default "";

  /**
   * @return the list of <a href="https://en.wikipedia.org/wiki/Subject_Alternative_Name">Subject Alternative Names</a> that
   *         should be included as part of the Order validation process.
   */
  String[] dnsNames() default {};

  /**
   * @return the list of IP address subjectAltNames to be set on the Certificate.
   */
  String[] ipAddresses() default {};

  /**
   * @return the list of URI subjectAltNames to be set on the Certificate.
   */
  String[] uris() default {};

  /**
   * @return the list of email subjectAltNames to be set on the Certificate.
   */
  String[] emailAddresses() default {};

  /**
   * @return the Keystores generation configuration.
   */
  CertificateKeystores keystores() default @CertificateKeystores;

  /**
   * @return if true, it will mark this Certificate as valid for certificate signing.
   */
  boolean isCA() default false;

  /**
   * @return the set of x509 usages that are requested for the certificate.
   */
  String[] usages() default {};

  /**
   * @return options to control private keys used for the Certificate.
   */
  CertificatePrivateKey privateKey() default @CertificatePrivateKey;

  /**
   * @return whether key usages should be present in the CertificateRequest
   */
  boolean encodeUsagesInRequest() default false;

  /**
   * @return the mount path where the generated certificate resources will be mounted.
   */
  String volumeMountPath() default "";

}
