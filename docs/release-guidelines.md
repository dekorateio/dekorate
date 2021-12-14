---
layout: base
title: Release Guidelines
subtitle: Release Guidelines
permalink: /release-guidelines/
---

# Release Guidelines

The project is a typical maven project that is released using the `maven-release-plugin`.

## Preparation

The project is released via sonatype. 
A sonatype account is required and the account needs to be added to https://issues.sonatype.org/browse/OSSRH-49341.

The credentials of that account need to be configured inside the local settings.xml (usually under ~/.m2/settings.xml).
The id of the repository is `oss-sonatype-staging`. So a matching server entry in the settings xml is required:


    <server>
      <id>oss-sonatype-staging</id>
      <username>your_username</username>
      <password>your_password</password>
    </server>
    
## Manual approach

As all maven projects, this project can be manually released with a command sequence, like:
    
    mvn release:clean release:prepare -Prelease -DpushChanges=false -Dtag=<release version>
    mvn release:perform -Prelease -DconnectionUrl=scm:git:file://`pwd`/.git -DreleaseVersion=<release_version> -Dtag=<release_version>
    
    
After these steps are performed:

- log in to https://oss.sonatype.org
- find the staging repository
- click `close`
- click `release`


Finally, you need to push commits and tags.

    git push origin
    git push --tags origin
    
    
## Automated approach

To automatically release the project, a release script is provided.
The script is meant to perfrom all the steps above, including detecting the next release version (next micro release), closing and releasing repositories.

To use the script simply run:

    ./release.sh
    
To bump the major or minor version:

    ./release.sh --release-version 0.X.0 --dev-version 0.X-SNAPSHOT
    
Note, that release and dev versions can be set individually, its just that we normalize SNAPSHOT to the major version point, to avoid constantly changing the dev version (and thus reducing potential scm conflicts).


## Cleanup

Whenever, the major version is changed, its a good idea, to also set the dev version of all modules that are not included in the release profile (e.g. examples and incubator modules).


     mvn versions:set -DnewVersion=0.7-SNAPSHOT -Pwith-examples -Pwith-incubator
