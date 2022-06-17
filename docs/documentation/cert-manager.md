---
title: Cert-Manager
description: Cert-Manager
layout: docs
permalink: /docs/cert-manager
---
### Cert-Manager

Dekorate supports generating an X.509 certificate with the help of the Certificate and Issuer CRD resources handled by the [Cert-Manager](https://cert-manager.io/). When these CRD resources are deployed on the cluster, the Cert-Manager will process them to populate a Secret containing for example a: CA certificate, private key, server certificate, or java keystores, etc.

To let Dekorate generate the certificate and issuer resources, simply declare the following dependency part of your pom file:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>certmanager-annotations</artifactId>
  <version>2.10.0</version>
</dependency>
```

And provide the certificate configuration. The minimal information that the Dekorate needs are:
- `secretName`: the name of the Kubernetes [Secret](https://kubernetes.io/docs/concepts/configuration/secret/) resource that will include the Cert-Manager generated files.
- the Issuer that represents the certificate authority (CA). See all the supported options in [the Issuer](#issuers) section.

For all the configuration options, please go to [the Configuration guide](https://dekorate.io/configuration-guide/#cert-manager) of the Cert-Manager.

The minimal configuration can be provided using the properties file and the following keys:

```
dekorate.certificate.secret-name=tls-secret
# The selfSigned issuer:
dekorate.certificate.self-signed.enabled=true
```

Or via the `@Certificate` annotation:

```java
@Certificate(secretName = "tls-secret", selfSigned = @SelfSigned(enabled = true))
public class Main {
    // ...
}
```

This configuration will generate up to two resources under the `target/classes/dekorate/kubernetes.yml` file that should look like this:

```yaml
---
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: kubernetes-example
spec:
  selfSigned: {}
---
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: kubernetes-example
spec:
  encodeUsagesInRequest: false
  isCA: false
  issuerRef:
    name: kubernetes-example
  secretName: tls-secret
```

Apart from these two resources, the Cert-Manager Dekorate extension will also configure, part of the Deployment, a volume mounted from the secret that contains the Cert-Manager generated files to allow the application to access them and to configure the HTTPS/TLS endpoint:

```yaml
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kubernetes-example
spec:
  replicas: 1
  template:
    spec:
      containers:
        - name: kubernetes-example
          volumeMounts:
            - mountPath: /etc/certs
              name: volume-certs
              readOnly: true
      volumes:
        - name: volume-certs
          secret:
            optional: false
            secretName: tls-secret
```

#### Usage

For an application (Quarkus, Spring Boot, ...) to be able to access the files mounted under `/etc/certs` from the secret, the application properties must also be updated. To see a practical working example, please go to [the Spring Boot with Cert-Manager](https://github.com/dekorateio/dekorate/tree/main/examples/spring-boot-with-certmanager-example) example which uses a PKCS Keystore.

#### Securing Resources

When securing your resources, it's important to validate that the requests are coming from known host names. For this purpose, we can use the `dnsNames` property which is part of the certificate configuration. For example, by adding the following `dekorate.certificate.dnsNames` property (it's a comma separated list of strings):

```
dekorate.certificate.dnsNames=foo.bar.com
```

The certificate will only allow requests accessing the server host `foo.bar.com`. Remark: If the DNS Host name does not exist, then you will get an error.

Note that the applications in Kubernetes can be publicly exposed using [Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/) resources, for example:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: kubernetes-example
spec:
  rules:
  - host: foo.bar.com
    http:
      paths:
      - pathType: Prefix
        path: "/"
        backend:
          service:
            name: kubernetes-example
            port:
              number: 8080
  tls:
    - hosts:
        - foo.bar.com
      secretName: tls-secret # < cert-manager will store the created certificate in this secret.
```

In Dekorate, you can generate the above Ingress resource by simply adding the following key properties:
```
dekorate.kubernetes.ingress.host=foo.bar.com
dekorate.kubernetes.ingress.expose=true
dekorate.kubernetes.ingress.tlsSecretName=tls-secret
```

#### Issuers

The `Issuer` is a Kubernetes resource that represents a certificate issuing authority that can generate signed certificates by honoring certificate signing requests. All cert-manager certificates require a referenced issuer to attempt to honor the request.

The supported issuers of this extension are SelfSigned, CA, Vault, and IssuerRef. 

**Note**: Only one issuer must be set between `selfSigned`, `ca`, `vault`, and `issuerRef`.

##### SelfSigned

Using the [SelfSigned issuer](https://cert-manager.io/docs/configuration/selfsigned/), the certificate will sign itself using the given private key.
To use the SelfSigned issuer, you need to add the following key property:
```
dekorate.certificate.selfSigned.enabled=true
```

##### CA

Using the [CA issuer](https://cert-manager.io/docs/configuration/ca/), the certificate and private key are stored inside the cluster as a Kubernetes Secret and will be used to sign incoming certificate requests.
To use the CA issuer, you need to add the following key properties:

```
dekorate.certificate.ca.secretName=ca-key-pair
```

When this certificate is installed in the cluster, Cert-Manager will issue the certificate and generate the CA secret resource `ca-key-pair` which the following content:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: ca-key-pair
data:
  tls.crt: <auto generated encrypted data>
  tls.key: <auto generated encrypted data>
```

##### Vault

Using the [Vault issuer](https://cert-manager.io/docs/configuration/vault/), the certificate will be issued by the certificate authority [Vault](https://www.vaultproject.io/).
To use the Vault issuer, you need the following key properties:

```
dekorate.certificate.vault.server=https://vault.example.com:8200
dekorate.certificate.vault.path=my_pki_mount/sign/my-role-name
# Any of the auth mechanisms to login into Vault:
## 1.- Via token secret resource reference:
dekorate.certificate.vault.authTokenSecretRef...
## 2.- Via using Application Role:
dekorate.certificate.vault.authAppRole...
## 3.- Via using Kubernetes service account:
dekorate.certificate.vault.authKubernetes...
```

##### Using a pre-existing issuer

To use a pre-existing issuer type that is separately installed in the cluster, you can use the `issuerRef` type. For example:

```
dekorate.certificate.issuerRef.name=my-issuer
dekorate.certificate.issuerRef.kind=ClusterIssuer
```

In this example, we are using a [ClusterIssuer](https://cert-manager.io/docs/concepts/issuer/) resource that is part of the Cert-Manager API and that should have previously been installed in the cluster.
