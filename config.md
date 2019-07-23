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
For example to reference the `httpActionPath` of the complex object `Probe` which is used to defined the `readinessProbe`:

    dekorate.kubernetes.readinessProbe.httpActionPath=/some/path


## Kubernetes
| Property                                        | Type                                          | Description | Default Value                            |
|-------------------------------------------------|-----------------------------------------------|-------------|------------------------------------------|
| dekorate.kubernetes.group                       | String                                        |             |                                          |
| dekorate.kubernetes.name                        | String                                        |             |                                          |
| dekorate.kubernetes.version                     | String                                        |             |                                          |
| dekorate.kubernetes.initContainers              | io.dekorate.kubernetes.annotation.Container[] |             |                                          |
| dekorate.kubernetes.labels                      | Label[]                                       |             |                                          |
| dekorate.kubernetes.annotations                 | Annotation[]                                  |             |                                          |
| dekorate.kubernetes.envVars                     | io.dekorate.kubernetes.annotation.Env[]       |             |                                          |
| dekorate.kubernetes.workingDir                  | String                                        |             |                                          |
| dekorate.kubernetes.command                     | String[]                                      |             |                                          |
| dekorate.kubernetes.arguments                   | String[]                                      |             |                                          |
| dekorate.kubernetes.replicas                    | int                                           |             | 1                                        |
| dekorate.kubernetes.serviceAccount              | String                                        |             |                                          |
| dekorate.kubernetes.host                        | String                                        |             |                                          |
| dekorate.kubernetes.ports                       | Port[]                                        |             |                                          |
| dekorate.kubernetes.serviceType                 | ServiceType                                   |             | ClusterIP                                |
| dekorate.kubernetes.pvcVolumes                  | PersistentVolumeClaimVolume[]                 |             |                                          |
| dekorate.kubernetes.secretVolumes               | SecretVolume[]                                |             |                                          |
| dekorate.kubernetes.configMapVolumes            | ConfigMapVolume[]                             |             |                                          |
| dekorate.kubernetes.gitRepoVolumes              | GitRepoVolume[]                               |             |                                          |
| dekorate.kubernetes.awsElasticBlockStoreVolumes | AwsElasticBlockStoreVolume[]                  |             |                                          |
| dekorate.kubernetes.azureDiskVolumes            | AzureDiskVolume[]                             |             |                                          |
| dekorate.kubernetes.azureFileVolumes            | AzureFileVolume[]                             |             |                                          |
| dekorate.kubernetes.mounts                      | Mount[]                                       |             |                                          |
| dekorate.kubernetes.imagePullPolicy             | ImagePullPolicy                               |             | IfNotPresent                             |
| dekorate.kubernetes.livenessProbe               | io.dekorate.kubernetes.annotation.Probe       |             | @io.dekorate.kubernetes.annotation.Probe |
| dekorate.kubernetes.readinessProbe              | io.dekorate.kubernetes.annotation.Probe       |             | @io.dekorate.kubernetes.annotation.Probe |
| dekorate.kubernetes.sidecars                    | io.dekorate.kubernetes.annotation.Container[] |             |                                          |
| dekorate.kubernetes.expose                      | boolean                                       |             | false                                    |
| dekorate.kubernetes.dockerFile                  | String                                        |             | Dockerfile                               |
| dekorate.kubernetes.registry                    | String                                        |             |                                          |
| dekorate.kubernetes.autoPushEnabled             | boolean                                       |             | false                                    |
| dekorate.kubernetes.autoBuildEnabled            | boolean                                       |             | false                                    |
| dekorate.kubernetes.autoDeployEnabled           | boolean                                       |             | false                                    |


## Openshift
| Property                                          | Type                                          | Description | Default Value                            |
|---------------------------------------------------|-----------------------------------------------|-------------|------------------------------------------|
| dekorate.openshift.group                          | String                                        |             |                                          |
| dekorate.openshift.name                           | String                                        |             |                                          |
| dekorate.openshift.version                        | String                                        |             |                                          |
| dekorate.openshift.initContainers                 | io.dekorate.kubernetes.annotation.Container[] |             |                                          |
| dekorate.openshift.labels                         | Label[]                                       |             |                                          |
| dekorate.openshift.annotations                    | Annotation[]                                  |             |                                          |
| dekorate.openshift.envVars                        | io.dekorate.kubernetes.annotation.Env[]       |             |                                          |
| dekorate.openshift.workingDir                     | String                                        |             |                                          |
| dekorate.openshift.command                        | String[]                                      |             |                                          |
| dekorate.openshift.arguments                      | String[]                                      |             |                                          |
| dekorate.openshift.replicas                       | int                                           |             | 1                                        |
| dekorate.openshift.serviceAccount                 | String                                        |             |                                          |
| dekorate.openshift.host                           | String                                        |             |                                          |
| dekorate.openshift.ports                          | Port[]                                        |             |                                          |
| dekorate.openshift.serviceType                    | ServiceType                                   |             | ClusterIP                                |
| dekorate.openshift.pvcVolumes                     | PersistentVolumeClaimVolume[]                 |             |                                          |
| dekorate.openshift.secretVolumes                  | SecretVolume[]                                |             |                                          |
| dekorate.openshift.configMapVolumes               | ConfigMapVolume[]                             |             |                                          |
| dekorate.openshift.gitRepoVolumes                 | GitRepoVolume[]                               |             |                                          |
| dekorate.openshift.awsElasticBlockStoreVolumes    | AwsElasticBlockStoreVolume[]                  |             |                                          |
| dekorate.openshift.azureDiskVolumes               | AzureDiskVolume[]                             |             |                                          |
| dekorate.openshift.azureFileVolumes               | AzureFileVolume[]                             |             |                                          |
| dekorate.openshift.mounts                         | Mount[]                                       |             |                                          |
| dekorate.openshift.imagePullPolicy                | ImagePullPolicy                               |             | IfNotPresent                             |
| dekorate.openshift.livenessProbe                  | io.dekorate.kubernetes.annotation.Probe       |             | @io.dekorate.kubernetes.annotation.Probe |
| dekorate.openshift.readinessProbe                 | io.dekorate.kubernetes.annotation.Probe       |             | @io.dekorate.kubernetes.annotation.Probe |
| dekorate.openshift.sidecars                       | io.dekorate.kubernetes.annotation.Container[] |             |                                          |
| dekorate.openshift.expose                         | boolean                                       |             | false                                    |
| dekorate.openshift.buildResourceGenerationEnabled | boolean                                       |             | true                                     |
| dekorate.openshift.builderImage                   | String                                        |             | fabric8/s2i-java:2.3                     |
| dekorate.openshift.buildEnvVars                   | io.dekorate.kubernetes.annotation.Env[]       |             |                                          |
| dekorate.openshift.autoBuildEnabled               | boolean                                       |             | false                                    |
| dekorate.openshift.autoDeployEnabled              | boolean                                       |             | false                                    |

### Openshift types

## Component

| Property                          | Type                                          | Description | Default Value                                  |
|-----------------------------------|-----------------------------------------------|-------------|------------------------------------------------|
| dekorate.component.name           | String                                        |             |                                                |
| dekorate.component.deploymentMode | DeploymentMode                                |             | dev                                            |
| dekorate.component.exposeService  | boolean                                       |             | false                                          |
| dekorate.component.envs           | io.dekorate.kubernetes.annotation.Env[]       |             |                                                |
| dekorate.component.buildType      | String                                        |             | s2i                                            |
| dekorate.component.remote         | String                                        |             | origin                                         |                           

#### Link
| Property      | Type                                    | Description | Default Value |
|---------------|-----------------------------------------|-------------|---------------|
| name          | String                                  |             |               |
| componentName | String                                  |             |               |
| kind          | Kind                                    |             | Env           |
| ref           | String                                  |             |               |
| envs          | io.dekorate.kubernetes.annotation.Env[] |             |               |

#### Capability
| Property   | Type        | Description | Default Value |
|------------|-------------|-------------|---------------|
| category   | String      |             |               |
| kind       | String      |             |               |
| name       | String      |             |               |
| version    | String      |             |               |
| parameters | Parameter[] |             |               |
#
## Global Types

The section below describes all the avialables subtypes
#### Annotation
| Property | Type   | Description | Default Value |
|----------|--------|-------------|---------------|
| key      | String |             |               |
| value    | String |             |               |
#### Label
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
| Property      | Type     | Description | Default Value |
|---------------|----------|-------------|---------------|
| name          | String   |             |               |
| containerPort | int      |             |               |
| hostPort      | int      |             | 0             |
| path          | String   |             | /             |
| protocol      | Protocol |             | TCP           |

#### Container
| Property        | Type                                    | Description | Default Value                            |
|-----------------|-----------------------------------------|-------------|------------------------------------------|
| image           | String                                  |             |                                          |
| name            | String                                  |             |                                          |
| envVars         | io.dekorate.kubernetes.annotation.Env[] |             |                                          |
| workingDir      | String                                  |             |                                          |
| command         | String[]                                |             |                                          |
| arguments       | String[]                                |             |                                          |
| ports           | Port[]                                  |             |                                          |
| mounts          | Mount[]                                 |             |                                          |
| imagePullPolicy | ImagePullPolicy                         |             | IfNotPresent                             |
| livenessProbe   | io.dekorate.kubernetes.annotation.Probe |             | @io.dekorate.kubernetes.annotation.Probe |
| readinessProbe  | io.dekorate.kubernetes.annotation.Probe |             | @io.dekorate.kubernetes.annotation.Probe |
#### Probe
| Property            | Type   | Description | Default Value |
|---------------------|--------|-------------|---------------|
| httpActionPath      | String |             |               |
| execAction          | String |             |               |
| tcpSocketAction     | String |             |               |
| initialDelaySeconds | int    |             |             0 |
| periodSeconds       | int    |             |            30 |
| timeoutSeconds      | int    |             |            10 |
#### Mount
| Property | Type    | Description | Default Value |
|----------|---------|-------------|---------------|
| name     | String  |             |               |
| path     | String  |             |               |
| subPath  | String  |             |               |
| readOnly | boolean |             | false         |
#### SecretVolume
| Property    | Type    | Description | Default Value |
|-------------|---------|-------------|---------------|
| volumeName  | String  |             |               |
| secretName  | String  |             |               |
| defaultMode | int     |             | 384           |
| optional    | boolean |             | false         |

#### AzureDiskVolume
| Property    | Type    | Description | Default Value |
|-------------|---------|-------------|---------------|
| volumeName  | String  |             |               |
| diskName    | String  |             |               |
| diskURI     | String  |             |               |
| kind        | String  |             | Managed       |
| cachingMode | String  |             | ReadWrite     |
| fsType      | String  |             | ext4          |
| readOnly    | boolean |             | false         |
####AzureFileVolume
| Property     | Type      | Description   | Default Value   |
|-- ---------- | --------- | ------------- | ------------- --|
| volumeName   | String    |               |                 |
| shareName    | String    |               |                 |
| secretName   | String    |               |                 |
| readOnly     | boolean   |               | false           |
#### AwsElasticBlockStoreVolume
| Property   | Type    | Description | Default Value |
|------------|---------|-------------|---------------|
| volumeName | String  |             |               |
| volumeId   | String  |             |               |
| partition  | int     |             |               |
| fsType     | String  |             | ext4          |
| readOnly   | boolean |             | false         |
#### GitRepoVolume
| Property   | Type   | Description | Default Value |
|------------|--------|-------------|---------------|
| volumeName | String |             |               |
| repository | String |             |               |
| directory  | String |             |               |
| revision   | String |             |               |
#### PersistentVolumeClaimVolume
| Property   | Type    | Description | Default Value |
|------------|---------|-------------|---------------|
| volumeName | String  |             |               |
| claimName  | String  |             |               |
| readOnly   | boolean |             | false         |
#### AzureDiskVolume
| Property     | Type      | Description   | Default Value   |
|-- ---------- | --------- | ------------- | ------------- --|
| volumeName   | String    |               |                 |
| diskName     | String    |               |                 |
| diskURI      | String    |               |                 |
| kind         | String    |               | Managed         |
| cachingMode  | String    |               | ReadWrite       |
| fsType       | String    |               | ext4            |
| readOnly     | boolean   |               | false           |
#### ConfigMapVolume
| Property        | Type      | Description   | Default Value   |
|-- ------------- | --------- | ------------- | ------------- --|
| volumeName      | String    |               |                 |
| configMapName   | String    |               |                 |
| defaultMode     | int       |               | 384             |
| optional        | boolean   |               | false           |

