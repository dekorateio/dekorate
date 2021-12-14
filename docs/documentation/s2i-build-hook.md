---
title: Source to Image Build Hook
description: Source to Image Build Hook
layout: docs
permalink: /docs/s2i-build-hook
---

### S2i build hook
This hook will just trigger an s2i binary build, that will pass the output folder as an input to the build

To enable the docker build hook you need:

- the `openshift-annotations` module (already included in all OpenShift starter modules)
- the `oc` binary configured to point the docker daemon of your kubernetes environment.

Finally, to trigger the hook, you need to pass `-Ddekorate.build=true`  as an argument to the build, for example:
```bash
mvn clean install -Ddekorate.build=true
```   
or if you are using gradle:
```bash
gradle build -Ddekorate.build=true  
```    
