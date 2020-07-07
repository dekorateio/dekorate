#!/bin/bash

#
# Script requires `grab`. To install it:
# curl -s https://raw.githubusercontent.com/shellib/grab/master/install.sh | bash
#

source $(grab github.com/shellib/cli)
source $(grab github.com/shellib/maven as maven)

release_version=$(readopt --release-version $*)
if [ -z $release_version ]; then
  echo "Option --release_version is required!"
  exit 1
fi

# Update the docs with the releae version
./scripts/ChangeVersion.java readme.md io.dekorate $release_version > readme.md.versionChanged
mv readme.md.versionChanged readme.md
git add readme.md
git commit -m "doc: Update dekorate version in docs to $release_version"
mvn versions:set -DnewVersion=$release_version -Pwith-examples
git add examples
git commit -m "chore: Set examples version to $release_version"
git stash save --keep-index --include-untracked
git stash drop
maven::release $*
