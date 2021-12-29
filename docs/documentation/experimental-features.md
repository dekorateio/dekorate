---
title: Experimental features
description: Experimental features
layout: docs
permalink: /docs/experimental-features
---
## Experimental features

Apart from the core feature, which is resource generation, there are a couple of experimental features that do add to the developer experience.

These features have to do with things like building, deploying and testing.

### Building and Deploying?
Dekorate does not generate Docker files, neither it provides internal support
for performing docker or s2i builds.
It does however allow the user to hook external tools (e.g. the `docker` or `oc`) to trigger container image builds after the end of compilation.

So, at the moment as an experimental feature the following hooks are provided:

- docker build hook (requires docker binary, triggered with `-Ddekorate.build=true`)
- docker push hook (requires docker binary, triggered with `-Ddekorate.push=true`)
- OpenShift s2i build hook (requires oc binary, triggered with `-Ddekorate.deploy=true`)
