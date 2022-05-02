---
title: Cert-Manager
description: Cert-Manager
layout: docs
permalink: /docs/cert-manager
---
### Cert-Manager

Dekorate supports generating `Certificate` and `Issuer` resources using [Cert-Manager](https://cert-manager.io/). When these resources are loaded into the cluster, Cert-Manager will issue the certificates, issuers, and trust-store files that then can be used by final applications.

To let Dekorate to generate the certificate and issuer resources, simply declare the following dependency part of your pom file:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>certmanager-annotations</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```

And provide the certificate configuration. The minimal information that the Dekorate needs are:
- `secretName` : the name of the Kubernetes [Secret](https://kubernetes.io/docs/concepts/configuration/secret/) resource that will contain all the generated files by Cert-Manager.
- the Issuer that represents certificate authorities (CAs). See all the supported options in [the Issuer](#issuers) section.

For all the configuration options, please go to [the Configuration guide](https://dekorate.io/configuration-guide/#cert-manager) for Cert-Manager.

To provide the minimal configuration, we can do it via the properties file:

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

Apart from these two resources, the Cert-Manager Dekorate extension will also configure the volumes that contain the generated files by Cert-Manager, so it's automatically accessible for applications:

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

After we configure the certificate properties, we need to update our applications to use the generated files by Cert-Manager that are mounted in the path `/etc/certs`.

To see a practical working example, please go to [the Spring Boot with Cert-Manager](https://github.com/dekorateio/dekorate/tree/main/examples/spring-boot-with-certmanager-example) example which uses a PKCS keystore.

#### Issuers

The `Issuer` is a Kubernetes resource that represents certificate authorities (CAs) that are able to generate signed certificates by honoring certificate signing requests. All cert-manager certificates require a referenced issuer that is in a ready condition to attempt to honor the request.

The supported issuers in this extension are:

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
