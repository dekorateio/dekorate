#!/bin/sh

# Copied from tekton catalog!
# Credits to: https://github.com/tektoncd/catalog/blob/master/jib-gradle/jib-gradle.yaml

set -o errexit
# Adds Gradle init script that applies the Jib Gradle plugin.
echo "initscript {
        repositories { maven { url 'https://plugins.gradle.org/m2' } }
        dependencies { classpath 'gradle.plugin.com.google.cloud.tools:jib-gradle-plugin:+' }
      }
      rootProject {
        afterEvaluate {
          if (!project.plugins.hasPlugin('com.google.cloud.tools.jib')) {
            project.apply plugin: com.google.cloud.tools.jib.gradle.JibPlugin
          }
        }
     }" > /tekton/home/init-script.gradle
