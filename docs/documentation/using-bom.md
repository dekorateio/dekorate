---
title: Using the BOM
description: using the BOM
layout: docs
permalink: /docs/using-bom
---
### Using the bom

Dekorate provides a bom, that offers dependency management for dekorate artifacts.

The bom can be imported like:

```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
               <groupId>io.dekorate</groupId>
               <artifactId>dekorate-bom</artifactId>
               <version>{{site.data.project.release.current-version}}</version>
               <type>pom</type>
               <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```

#### Using with downstream BOMs

In case, that dekorate bom is imported by a downstream project (e.g. snowdrop) and its required to override the bom version, all you need to do is to import the dekorate bom with the version of your choice first.
