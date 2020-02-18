# Configuration Options
This page describes all the available configuration options.

All options start with the prefix `dekorate`. 
Each option can be:

- array
- primitive
- string
- enum value
- complex type

## Arrays
They can be referenced by index inside brackets as in java for example: `dekorate.kubernetes.labels[0]`.

## Complex object
Each property of the complex object can be specified, by expanding the the property key.
For example lets assume the object `Probe` that looks like:

| Property              | Type   | Description | Default Value |
|-----------------------|--------|-------------|---------------|
| http-action-path      | String |             |               |
| exec-action           | String |             |               |
| tcp-socket-action     | String |             |               |
| initial-delay-seconds | int    |             |             0 |
| period-seconds        | int    |             |            30 |
| timeout-seconds       | int    |             |            10 |


To reference the `http-action-aath` of the complex object `Probe` which is used to define the `readiness-probe` property of `Kubernetes`:

    dekorate.kubernetes.readiness-probe.http-action-path=/some/path

## Casing

Please note that the casings for these properties are:

- camel case
- kebab

For example both of `dekorate.kubernetes.initCcontainers` and `dekorate.kubernetes.init-containers` are supported.
This document uses kebab. 

Below is a list of all the available options.
The document is structured as follows.

- [kubernetes](#kubernetes)
  - [global types](#global-types)
  - [mounts and voulumes](#mounts-and-volumes)
- [openshift](#openshift)
- [knative](#knative)
- [halkyon](#halkyon-types)

## Kubernetes
| Property                                            | Type                          | Description | Default Value |
|-----------------------------------------------------|-------------------------------|-------------|---------------|
| dekorate.kubernetes.group                           | String                        |             |               |
| dekorate.kubernetes.name                            | String                        |             |               |
| dekorate.kubernetes.version                         | String                        |             |               |
| dekorate.kubernetes.init-containers                 | Container[]                   |             |               |
| dekorate.kubernetes.labels                          | Label[]                       |             |               |
| dekorate.kubernetes.annotations                     | Annotation[]                  |             |               |
| dekorate.kubernetes.env-vars                        | Env[]                         |             |               |
| dekorate.kubernetes.working-dir                     | String                        |             |               |
| dekorate.kubernetes.command                         | String[]                      |             |               |
| dekorate.kubernetes.arguments                       | String[]                      |             |               |
| dekorate.kubernetes.replicas                        | int                           |             | 1             |
| dekorate.kubernetes.service-account                 | String                        |             |               |
| dekorate.kubernetes.host                            | String                        |             |               |
| dekorate.kubernetes.ports                           | Port[]                        |             |               |
| dekorate.kubernetes.service-type                    | ServiceType                   |             | ClusterIP     |
| dekorate.kubernetes.pvc-volumes                     | PersistentVolumeClaimVolume[] |             |               |
| dekorate.kubernetes.secret-volumes                  | SecretVolume[]                |             |               |
| dekorate.kubernetes.config-map-volumes              | ConfigMapVolume[]             |             |               |
| dekorate.kubernetes.git-repo-volumes                | GitRepoVolume[]               |             |               |
| dekorate.kubernetes.aws-elastic-block-store-volumes | AwsElasticBlockStoreVolume[]  |             |               |
| dekorate.kubernetes.azure-disk-volumes              | AzureDiskVolume[]             |             |               |
| dekorate.kubernetes.azure-file-volumes              | AzureFileVolume[]             |             |               |
| dekorate.kubernetes.mounts                          | Mount[]                       |             |               |
| dekorate.kubernetes.image-pull-policy               | ImagePullPolicy               |             | IfNotPresent  |
| dekorate.kubernetes.image-pull-secrets              | String[]                      |             |               |
| dekorate.kubernetes.liveness-probe                  | Probe                         |             | ( see Probe ) |
| dekorate.kubernetes.readiness-probe                 | Probe                         |             | ( see Probe ) |
| dekorate.kubernetes.sidecars                        | Container[]                   |             |               |
| dekorate.kubernetes.expose                          | boolean                       |             | false         |
| dekorate.kubernetes.auto-deploy-enabled             | boolean                       |             | false         |

### Global Types
The section below describes all the available subtypes.

#### Label
| Property | Type   | Description | Default Value |
|----------|--------|-------------|---------------|
| key      | String |             |               |
| value    | String |             |               |

#### Annotation
| Property | Type   | Description | Default Value |
|----------|--------|-------------|---------------|
| key      | String |             |               |
| value    | String |             |               |


#### Env
| Property  | Type   | Description | Default Value |
|-----------|--------|-------------|---------------|
| name      | String |             |               |
| value     | String |             |               |
| secret    | String |             |               |
| configmap | String |             |               |
| field     | String |             |               |

#### Port
| Property       | Type     | Description | Default Value |
|----------------|----------|-------------|---------------|
| name           | String   |             |               |
| container-port | int      |             |               |
| host-port      | int      |             | 0             |
| path           | String   |             | /             |
| protocol       | Protocol |             | TCP           |

#### Container
| Property          | Type            | Description | Default Value |
|-------------------|-----------------|-------------|---------------|
| image             | String          |             |               |
| name              | String          |             |               |
| env-vars          | Env[]           |             |               |
| working-dir       | String          |             |               |
| command           | String[]        |             |               |
| arguments         | String[]        |             |               |
| ports             | Port[]          |             |               |
| mounts            | Mount[]         |             |               |
| image-pull-policy | ImagePullPolicy |             | IfNotPresent  |
| liveness-probe    | Probe           |             | ( see Probe ) |
| readiness-probe   | Probe           |             | ( see Probe ) |

#### Probe
| Property              | Type   | Description | Default Value |
|-----------------------|--------|-------------|---------------|
| http-action-path      | String |             |               |
| exec-action           | String |             |               |
| tcp-socket-action     | String |             |               |
| initial-delay-seconds | int    |             |             0 |
| period-seconds        | int    |             |            30 |
| timeout-seconds       | int    |             |            10 |
| success-threshold     | int    |             |             1 |
| failure-threshold     | int    |             |             3 |

### Mounts and Volumes
#### Mount
| Property  | Type    | Description | Default Value |
|-----------|---------|-------------|---------------|
| name      | String  |             |               |
| path      | String  |             |               |
| sub-path  | String  |             |               |
| read-only | boolean |             | false         |

#### ConfigMapVolume
| Property        | Type    | Description | Default Value |
|-----------------|---------|-------------|---------------|
| volume-name     | String  |             |               |
| config-map-name | String  |             |               |
| default-mode    | int     |             | 384           |
| optional        | boolean |             | false         |

#### SecretVolume
| Property     | Type    | Description | Default Value |
|--------------|---------|-------------|---------------|
| volume-name  | String  |             |               |
| secret-name  | String  |             |               |
| default-mode | int     |             | 384           |
| optional     | boolean |             | false         |

#### PersistentVolumeClaimVolume
| Property    | Type    | Description | Default Value |
|-------------|---------|-------------|---------------|
| volume-name | String  |             |               |
| claim-name  | String  |             |               |
| read-only   | boolean |             | false         |


#### GitRepoVolume
| Property    | Type   | Description | Default Value |
|-------------|--------|-------------|---------------|
| volume-name | String |             |               |
| repository  | String |             |               |
| directory   | String |             |               |
| revision    | String |             |               |

#### AwsElasticBlockStoreVolume
| Property    | Type    | Description | Default Value |
|-------------|---------|-------------|---------------|
| volume-name | String  |             |               |
| volume-id   | String  |             |               |
| partition   | int     |             |               |
| fs-type     | String  |             | ext4          |
| read-only   | boolean |             | false         |

#### AzureDiskVolume
| Property    | Type    | Description | Default Value |
|-------------|---------|-------------|---------------|
| volume-name | String  |             |               |
| share-name  | String  |             |               |
| secret-name | String  |             |               |
| read-only   | boolean |             | false         |

#### AzureDiskVolume
| Property     | Type    | Description | Default Value |
|--------------|---------|-------------|---------------|
| volume-name  | String  |             |               |
| disk-name    | String  |             |               |
| disk-u-r-i   | String  |             |               |
| kind         | String  |             | Managed       |
| caching-mode | String  |             | ReadWrite     |
| fs-type      | String  |             | ext4          |
| read-only    | boolean |             | false         |


## Docker
| Property                            | Type    | Description | Default Value |
|-------------------------------------|---------|-------------|---------------|
| dekorate.docker.docker-file         | String  |             | Dockerfile    |
| dekorate.docker.registry            | String  |             |               |
| dekorate.docker.auto-push-enabled   | boolean |             | false         |
| dekorate.docker.auto-build-enabled  | boolean |             | false         |
| dekorate.docker.auto-deploy-enabled | boolean |             | false         |

## Openshift
| Property                                           | Type                          | Description | Default Value |
|----------------------------------------------------|-------------------------------|-------------|---------------|
| dekorate.openshift.group                           | String                        |             |               |
| dekorate.openshift.name                            | String                        |             |               |
| dekorate.openshift.version                         | String                        |             |               |
| dekorate.openshift.init-containers                 | Container[]                   |             |               |
| dekorate.openshift.labels                          | Label[]                       |             |               |
| dekorate.openshift.annotations                     | Annotation[]                  |             |               |
| dekorate.openshift.env-vars                        | Env[]                         |             |               |
| dekorate.openshift.working-dir                     | String                        |             |               |
| dekorate.openshift.command                         | String[]                      |             |               |
| dekorate.openshift.arguments                       | String[]                      |             |               |
| dekorate.openshift.replicas                        | int                           |             | 1             |
| dekorate.openshift.service-account                 | String                        |             |               |
| dekorate.openshift.host                            | String                        |             |               |
| dekorate.openshift.ports                           | Port[]                        |             |               |
| dekorate.openshift.service-type                    | ServiceType                   |             | ClusterIP     |
| dekorate.openshift.pvc-volumes                     | PersistentVolumeClaimVolume[] |             |               |
| dekorate.openshift.secret-volumes                  | SecretVolume[]                |             |               |
| dekorate.openshift.config-map-volumes              | ConfigMapVolume[]             |             |               |
| dekorate.openshift.git-repo-volumes                | GitRepoVolume[]               |             |               |
| dekorate.openshift.aws-elastic-block-store-volumes | AwsElasticBlockStoreVolume[]  |             |               |
| dekorate.openshift.azure-disk-volumes              | AzureDiskVolume[]             |             |               |
| dekorate.openshift.azure-file-volumes              | AzureFileVolume[]             |             |               |
| dekorate.openshift.mounts                          | Mount[]                       |             |               |
| dekorate.openshift.image-pull-policy               | ImagePullPolicy               |             | IfNotPresent  |
| dekorate.openshift.image-pull-secrets              | String[]                      |             |               |
| dekorate.openshift.liveness-probe                  | Probe                         |             | ( see Probe ) |
| dekorate.openshift.readiness-probe                 | Probe                         |             | ( see Probe ) |
| dekorate.openshift.sidecars                        | Container[]                   |             |               |
| dekorate.openshift.expose                          | boolean                       |             | false         |
| dekorate.openshift.auto-deploy-enabled             | boolean                       |             | false         |

### S2i
| Property                     | Type    | Description | Default Value        |
|------------------------------|---------|-------------|----------------------|
| dekorate.s2i.enabled         | boolean |             | true                 |
| dekorate.docker-file         | String  |             | Dockerfile           |
| dekorate.registry            | String  |             |                      |
| dekorate.builder-image       | String  |             | fabric8/s2i-java:2.3 |
| dekorate.build-env-vars      | Env[]   |             |                      |
| dekorate.auto-push-enabled   | boolean |             | false                |
| dekorate.auto-build-enabled  | boolean |             | false                |

## Knative

| Property                                         | Type                          | Description | Default Value |
|--------------------------------------------------|-------------------------------|-------------|---------------|
| dekorate.knative.group                           | String                        |             |               |
| dekorate.knative.name                            | String                        |             |               |
| dekorate.knative.version                         | String                        |             |               |
| dekorate.knative.labels                          | Label[]                       |             |               |
| dekorate.knative.annotations                     | Annotation[]                  |             |               |
| dekorate.knative.env-vars                        | Env[]                         |             |               |
| dekorate.knative.working-dir                     | String                        |             |               |
| dekorate.knative.command                         | String[]                      |             |               |
| dekorate.knative.arguments                       | String[]                      |             |               |
| dekorate.knative.service-account                 | String                        |             |               |
| dekorate.knative.host                            | String                        |             |               |
| dekorate.knative.ports                           | Port[]                        |             |               |
| dekorate.knative.service-type                    | ServiceType                   |             | ClusterIP     |
| dekorate.knative.pvc-volumes                     | PersistentVolumeClaimVolume[] |             |               |
| dekorate.knative.secret-volumes                  | SecretVolume[]                |             |               |
| dekorate.knative.config-map-volumes              | ConfigMapVolume[]             |             |               |
| dekorate.knative.git-repo-volumes                | GitRepoVolume[]               |             |               |
| dekorate.knative.aws-elastic-block-store-volumes | AwsElasticBlockStoreVolume[]  |             |               |
| dekorate.knative.azure-disk-volumes              | AzureDiskVolume[]             |             |               |
| dekorate.knative.azure-file-volumes              | AzureFileVolume[]             |             |               |
| dekorate.knative.mounts                          | Mount[]                       |             |               |
| dekorate.knative.image-pull-policy               | ImagePullPolicy               |             | IfNotPresent  |
| dekorate.knative.image-pull-secrets              | String[]                      |             |               |
| dekorate.knative.liveness-probe                  | Probe                         |             | ( see Probe ) |
| dekorate.knative.readiness-probe                 | Probe                         |             | ( see Probe ) |
| dekorate.knative.sidecars                        | Container[]                   |             |               |
| dekorate.knative.expose                          | boolean                       |             | false         |
| dekorate.knative.auto-deploy-enabled             | boolean                       |             | false         |

## Halkyon types

This secrtion describes all halkyon related types.

### HalkyonComponent
| Property                     | Type   | Description | Default Value |
|------------------------------|--------|-------------|---------------|
| dekorate.link.name           | String |             |               |
| dekorate.link.component-name | String |             |               |
| dekorate.link.type           | Type   |             | Env           |
| dekorate.link.ref            | String |             |               |
| dekorate.link.envs           | Env[]  |             |               |
|                              |        |             |               |

#### HalkyonCapability
| Property                       | Type        | Description | Default Value |
|--------------------------------|-------------|-------------|---------------|
| dekorate.capability.category   | String      |             |               |
| dekorate.capability.type       | String      |             |               |
| dekorate.capability.name       | String      |             |               |
| dekorate.capability.version    | String      |             |               |
| dekorate.capability.parameters | Parameter[] |             |               |

