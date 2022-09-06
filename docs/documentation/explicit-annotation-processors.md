---
title: Annotation Processors
description: Explicit configuration of annotation processors
layout: docs
permalink: /docs/annotation-processors
---

### Explicit configuration of annotation processors

By default, Dekorate doesn't require any specific configuration of its annotation processors.
However, it is possible to manually define the annotation processors if
required.

In the maven pom.xml configure the annotation processor path in the maven compiler plugin settings.

The example below configures the Mapstruct, Lombok and Dekorate annotation processors

```xml
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${maven-compiler-plugin.version}</version>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>io.dekorate</groupId>
                            <artifactId>kubernetes-annotations</artifactId>
                            <version>{{site.data.project.release.current-version}}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin> 
```
