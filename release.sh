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

function set_version() {
  local file=$1
  ./scripts/ChangeVersion.java ${file} io.dekorate $release_version > ${file}.versionChanged
  mv ${file}.versionChanged ${file} 
  git add ${file}
}

# Update the docs with the release version
set_version readme.md io.dekorate $release_version
ls docs/documentation/*.md | while read doc; do
  set_version $doc io.dekorate $release_version
done

git commit -m "doc: Update dekorate version in docs to $release_version"
mvn versions:set -DnewVersion=$release_version -Pwith-examples
git add examples
git commit -m "chore: Set examples version to $release_version"
git stash save --keep-index --include-untracked
git stash drop
maven::release $*
