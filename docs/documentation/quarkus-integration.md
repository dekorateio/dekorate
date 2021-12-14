---
title: Quarkus integration
description:  Quarkus integration
layout: docs
permalink: /docs/quarkus-integration
---

#### Quarkus

[quarkus](https://quarkus.io) provides rich set of [extensions](https://quarkus.io/extensions) including one for [kubernetes](https://quarkus.io/guides/deploying-to-kubernetes).
The [kubernetes extension](https://quarkus.io/guides/deploying-to-kubernetes) uses internally [dekorate](https://github.com/dekorateio/dekorate) for generating and customizing manifests.

The extension can be added to any [quarkus](https://quarkus.io) project:

    mvn quarkus:add-extension -Dextensions="io.quarkus:quarkus-kubernetes"

After the project compilation the generated manifests will be available under: `target/kubernetes/`.

At the moment this extension will handle ports, health checks etc, with zero configuration from the user side.

It's important to note, that by design this extension will NOT use the [dekorate](https://github.com/dekorateio/dekorate) annotations for customizing the generated manifests.

For more information please check: the extension [docs](https://quarkus.io/guides/deploying-to-kubernetes).
