---
layout: base
title: Contribute
subtitle: Feel free to contribute in any way you like docs, suggestions, features, fixes tests.
permalink: /contribute/
---

{% include title-band.html %}

# Contributor guidelines

To make both your and our life easier here are some tips:

## editorconfig

To make sure that regardless of IDE/editor everyone uses the same settings we use `editorconfig`.
Just make sure that you have the `editorconfig` plugin for your editor installed.

## configure IDE

* use the `editorconfig`
* on the root level `mvn package` which also generates all sources and domains
* add `target/generated-sources/annotations` as source in all projects where the `io.dekorate.application.config` domain classes are needed (e.g. all annotation projects, some test projects etc)


## pull request scope

Keep your pull requests as small as possible.
Please, avoid combining code with indentation changes.

## semantic messages

Please use [semantic commit messages](https://seesparkbox.com/foundry/semantic_commit_messages).

## Tests and documentation are not optional

1. All pull requests must contain unit tests so don't forget to include tests in your pull requests. The unit tests should be added in the corresponding `src/test` folder of the modified module.
1. When fixing an issue a test case should be added in the [tests](https://github.com/dekorateio/dekorate/tree/master/tests) folder. Navigate to the `tests` folder and add a new folder named as follows: `feat/issue-github_issue_number-issue_description`, then add the code inside it.
1. Check if some of the [examples](https://github.com/dekorateio/dekorate/tree/master/examples) should be modified.
1. Also don't forget the documentation (reference documentation, javadoc...).

## Versions and Branches

The current version of dekorate is `<version>2.0.0</version>`.

### What's changed in 2.x

Most of the changes that happend inside 2.x are internal and are related to the maintainance of the project.

#### New features

- Configurable logging threshold
- Git options
- Inferring image configuration from application config
- JaxRS support (without requiring Thorntail)
- Integration testing framework improvements (detailed diagnostics on error)
- Updated to kubernetes-client and model v5.1.1

#### Annotation naming

- EnableDockerBuild -> DockerBuild
- EnableS2iBuild -> S2iBuild
- EnableJibBuild -> JibBuild

#### Dropped modules

The following features were dropped:

- service catalog
- halkyon
- application crd
- crd generator (functionality moved to the fabric8 kubernetes-client).
- dependencies uberjar

#### Dropped `dependencies` shadowed uber jar

Earlier version of dekorate used a shadowed uberjar containing all dependencies.
As of `2.0.0` the `dependencies` uberjar is no more.
Downstream projects using dekorate as a library will need to switch from `io.dekorate.deps.xxx` to the original packages.

#### Component naming

Earlier version of dekorate used names for its core components that we too generic.
So, in 2.0.0 the name changed so that they are more descriptive.
Naming changes:

- Generator -> ConfigGenerator
- Hanlder -> ManifestGenerator

### Branches

All dekorate development takes place on the `master` branch. From that branch `current` releases are created.
Bug fixes for older releases are done through their correspnding branch.

- master (active development, pull requests should point here)
- 1.0.x
- 0.15.x

### Pull request guidelines

All pull requests should target the `main` branch and from there things are backported to where it makes sense.


## Frequently Asked Questions

### IntelliJ fails to compile dekorate with `Cannot resolve method 'withName(java.lang.String)'` and this kind of errors.
In order to get Dekorate built on IntelliJ you need to manually add generated sources as module sources.


### How do I tell dekorate to use a custom image name?

By default the image name used is `${group}/${name}:${version}` as extracted by the project / environment or explicitly configured by the user.
If you don't want to tinker those properties then you can:

#### Using annotations

Add `@DockerBuild(image="foo/bar:baz")` to the your main or whatever class you use to configure dekorate. If instead of docker you are using jib or s2i you can use `@JibBuild(image="foo/bar:baz")` or `@S2iBuild(image="foo/bar:baz")` respectively.

#### Using annotations

Add the following to your application.properties

```
dekorate.docker.image=foo/bar:baz
```

#### Using annotations

Add the following to your application.yaml

```
dekorate:
  docker:
    image: foo/bar:baz
```

#### related examples
- [kubernetes with custom image name example](https://github.com/dekorateio/dekorate/tree/main/examples/kubernetes-with-custom-image-name-example)


### Release Guidelines
Please, check our [release guidelines]({{site.baseurl}}/release-guidelines)

[comment]: <> (## Want to get involved?)

[comment]: <> (By all means please do! We love contributions!)

[comment]: <> (Docs, Bug fixes, New features ... everything is important!)

[comment]: <> (Make sure you take a look at contributor [guidelines]&#40;assets/contributor-guidelines.md&#41;.)

[comment]: <> (Also, it can be useful to have a look at the dekorate [design]&#40;assets/design.md&#41;.)

