---
title: Debugging and Logging
description: Debugging and Logging
layout: docs
permalink: /docs/debugging-logging
---

### Debugging and Logging

To control how verbose the Dekorate output is going to be you can set the log level threshold, using the `io.dekorate.log.level` system property-drawer.

Allowed values are:

- OFF
- ERROR
- WARN
- INFO (default)
- DEBUG

### Troubleshooting

Additionally, we can turn on the full verbose mode to troubleshoot issues at the Dekorate generation of manifests by providing the system property `dekorate.verbose=true`. 

Currently, this mode will print:
- The configuration registries applied
- The decorators applied
