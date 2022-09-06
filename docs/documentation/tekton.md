---
title: Tekton
description: Tekton
layout: docs
permalink: /docs/tekton
---
### Tekton

Dekorate supports generating `tekton` pipelines.
Since Dekorate knows, how your project is build, packaged into containers and
deployed, converting that knowledge into a pipeline comes natural.

When the `tekton` module is added to the project:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>tekton-annotations</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```

Two sets of resources will be generated, each representing a different configuration style the use user can choose from:

- Pipeline based
  - tekton-pipeline.yml
  - tekton-pipeline-run.yml
  - tekton-pipeline.json
  - tekton-pipeline-run.json
- Task based
  - tekton-task.yml
  - tekton-task-run.yml
  - tekton-task.json
  - tekton-task-run.json

#### Pipeline

This set of resources contains:

- Pipeline
- PipelineResource (git, output image)
- PipelineRun
- Task (build, package and push, deploy)
- RBAC resources

These are the building blocks of a Tekton pipeline that grabs your project from
scm, builds and containerizes the project (in cluster) and finally deploys it.

#### Task

This set of resources provides the some functionality as above, but everything
is collapsed under a single task (for usability reasons), In detail it contains:

- PipelineResource (git, output image)
- Task
- TaskRun
- RBAC resources

#### Pipeline vs Task

If unsure which style to pickup, note that the `task` style has less
configuration requirements and thus easier to begin with. The `pipeline` style
is easier to slice and dice, once your are more comfortable with `tekton`.

Regardless of the choice, Dekorate provides a rich set of configuration options
to make using `tekton` as easy as it gets.

#### Tekton Configuration

##### Git Resource

The generated tasks and pipelines, assume the project is under version control and more specifically git.
So, in order to `run` the pipeline or the `task` a `PiepelineResource` of type `git` is required.
If the project is added to git, the resource will be generated for you. If for any reason the use of an external resource is
preferred then it needs to be configured, like:

```
dekorate.tekton.external-git-pipeline-resource=<<the name of the resource goes here>>
```

##### Builder Image

Both the pipeline and the task based resources include steps that perform a
build of the project. Dekorate, tries to identify a suitable builder image for
the project. Selection is based on the build tool, jdk version, jdk flavor and
build tool version (in that order). At the moment only maven and gradle are supported.

You can customize the build task by specifying:

- custom builder image: `dekorate.tekton.builder-image`
- custom build command: `dekorate.tekton.builder-command`
- custom build arguments: `dekorate.tekton.builder-arguments`

##### Configuring a Workspace PVC

One of the main differences between the two styles of configuration, is that
Pipelines require a `PersistentVolumeClaim` in order to share the workspace
between Tasks. On the contrary when all steps are part of single bit fat Task
(which is baked by a Pod) and `EmptyDir` volume will suffice.

Out of the box, for the pipeline style resources a `PersistentVolumeClaim` named
after the application will be generated and used.

The generated pvc can be customized using the following properties:

- dekorate.tekton.source-workspace-size (defaults to `1Gi`)
- dekorate.tekton.source-workspace-storage-class (defaults to `standard`)

The option to provide an existing pvc (by name) instead of generating one is also
provided, using `dekorate.tekton.source-workspace-claim`.

##### Configuring the Docker registry for Tekton

The generated Pipeline / Task includes steps for building a container image and
pushing it to a registry.

The registry can be configured using `dekorate.docker.registry` as is done for
the rest of the resources.

For the push to succeed credentials for the registry are required.
The user is able to:

- Provide own Secret with registry credentials
- Provide username and password
- Upload local `.docker/config.json`

To provide an existing secret for the job (e.g. `my-secret`):

```
dekorate.tekton.image-builder-secert=my-secert
```

To provide username and password:

```
dekorate.tekton.registry-usernmae=myusername
dekorate.tekton.registry-password=mypassword
```

If none of the above is provided and a `.docker/config.json` exists, it can be
used if explicitly requested:

```
dekorate.tekton.use-local-docker-config-json=true
```


