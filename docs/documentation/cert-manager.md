---
title: Cert-Manager
description: Cert-Manager
layout: docs
permalink: /docs/cert-manager
---
### Cert-Manager

Dekorate supports to generate a X.509 certificate with the help of the Certificate and Issuer CRD resources handled by the [Cert-Manager](https://cert-manager.io/). When these CRD resources are deployed on the cluster, the Cert-Manager will process them in order to populate a Secret containing by example a: CA certificate, private key, server certificate or java keystores, etc.

To let Dekorate to generate the certificate and issuer resources, simply declare the following dependency part of your pom file:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>certmanager-annotations</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```

And provide the certificate configuration. The minimal information that the Dekorate needs is:
- `secretName` : the name of the Kubernetes [Secret](https://kubernetes.io/docs/concepts/configuration/secret/) resource that will include the Cert-Manager generated files.
- the Issuer that represents certificate authorities (CAs). See all the supported options in [the Issuer](#issuers) section.

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

This configuration will generate up to two resources under `target/classes/dekorate/kubernetes.yml` file that should look like this:

```
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

Apart from these two resources, the Cert-Manager Dekorate extension will also configure, part of the Deployment, a volume mounted from the secret that contain the Cert-Manager generated files to allow the application to access them and to configure the HTTPS/TLS endpoint:

```
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

To let the application consume the path mounted, we have then to update also the properties of the application (Quarkus, Spring Boot, ...) to use Cert-Manager the generated files that are mounted in the folder `/etc/certs`.

To see a practical working example, please go to [the Spring Boot with Cert-Manager](https://github.com/dekorateio/dekorate/tree/main/examples/spring-boot-with-certmanager-example) example which uses a PKCS keystore.

#### Issuers

The `Issuer` is a Kubernetes resource that represents a certificate issuing authority that are able to generate signed certificates by honoring certificate signing requests. All cert-manager certificates require a referenced issuer that is in a ready condition to attempt to honor the request.

The supported issuers of this extension are:

- [SelfSigned](https://cert-manager.io/docs/configuration/selfsigned/)
- [CA](https://cert-manager.io/docs/configuration/ca/)
- [Vault](https://cert-manager.io/docs/configuration/vault/)

Alternatively, you can use an Issuer reference that is already installed in the cluster via the properties file:

```
dekorate.certificate.issuerRef.name=my-issuer
dekorate.certificate.issuerRef.kind=ClusterIssuer
```

In this example, we are using a [ClusterIssuer](https://cert-manager.io/docs/concepts/issuer/) resource that is part of the Cert-Manager API.

**Note**: Only one issuer must be set between `selfSigned`, `ca`, `vault` and `issuerRef`
