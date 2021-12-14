---
title: Usage
description: Dekorate usage
layout: docs
permalink: /docs/usage
---

## Usage

To start using this project you just need to add one of the provided dependencies to your project.
For known frameworks like [spring boot](https://spring.io/projects/spring-boot), [quarkus](https://quarkus.io), or [thorntail](https://thorntail.io) that's enough.
For generic java projects, we also need to add an annotation that expresses our intent to enable `dekorate`.

This annotation can be either [@Dekorate](core/src/main/java/io/dekorate/annotation/Dekorate.java) or a more specialized one, which also gives us access to more specific configuration options.
Further configuration is feasible using:

- Java annotations
- Configuration properties (application.properties)
- Both

A complete reference of the supported properties can be found in the [configuration options guide]({{site.baseurl}}/configuration-guide/).

