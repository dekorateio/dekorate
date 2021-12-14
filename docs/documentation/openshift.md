---
title: Openshift
description: Openshift
layout: docs
permalink: /docs/openshift
---
### Openshift

## Setting up

Add the following dependency to your project:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>openshift-annotations</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```

Then add the `@Dekorate` annotation to one of your Java source files. 

```java
package org.acme;

import io.dekorate.annotation.Dekorate;

@Dekorate
public class Application {
}
```

Note: It doesn't have to be the `Main` class.
Next time you perform a build, using something like:

## Building

    mvn clean package
    
The generated manifests can be found under `target/classes/META-INF/dekorate`.

## Configuration styles

The generated manifests can be customized either using annotations or configuration properties/yml.

### Using annotations

The `@OpenshiftApplication` under `io.dekorate.openshift.annotation` is a `@Dekorate` alternative through which
the user can specify all sorts of customization, For example to set the replicas to 2:

```java
package org.acme;

import io.dekorate.openshift.annotation.OpenshiftApplication;

@OpenshiftApplication(replicas=2)
public class Application {
}
```

### Using framework configuration

The same can be achieved using plain old configuration (e.g.`application.properties`):

```
dekorate.openshift.replicas=2
```

**A complete reference on all the supported properties can be found in the [configuration options guide](/{{site.baseurl}}/configuration-guide).
