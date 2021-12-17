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


## Frequently Asked Questions

* IntelliJ fails to compile dekorate with `Cannot resolve method 'withName(java.lang.String)'` and this kind of errors.
  In order to get dekorate built on IntelliJ you need to manually add generated sources as module sources.
