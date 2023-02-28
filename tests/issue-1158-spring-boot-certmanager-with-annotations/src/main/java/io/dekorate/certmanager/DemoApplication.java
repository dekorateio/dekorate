/**
 * Copyright 2018 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.certmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.dekorate.certmanager.annotation.Certificate;
import io.dekorate.certmanager.annotation.CertificateKeystore;
import io.dekorate.certmanager.annotation.CertificateKeystores;
import io.dekorate.certmanager.annotation.CertificatePrivateKey;
import io.dekorate.certmanager.annotation.LocalObjectReference;
import io.dekorate.certmanager.annotation.PrivateKeyAlgorithm;
import io.dekorate.certmanager.annotation.PrivateKeyEncoding;
import io.dekorate.certmanager.annotation.SelfSigned;
import io.dekorate.certmanager.annotation.Subject;

@SpringBootApplication
@Certificate(secretName = "tls-secret", selfSigned = @SelfSigned(enabled = true), usages = { "server auth",
    "client auth" }, dnsNames = { "kubernetes-example.com", "localhost" }, subject = @Subject(organizations = { "Dekorate",
        "Community" }), duration = "2160h0m0s", renewBefore = "360h0m0s", privateKey = @CertificatePrivateKey(algorithm = PrivateKeyAlgorithm.RSA, encoding = PrivateKeyEncoding.PKCS8, size = 2048), keystores = @CertificateKeystores(pkcs12 = @CertificateKeystore(create = true, passwordSecretRef = @LocalObjectReference(name = "pkcs12-pass", key = "password"))))
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

}
