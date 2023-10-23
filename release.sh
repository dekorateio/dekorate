#!/bin/bash

if [ ! -f "$HOME/bin/grab" ]; then
  mkdir -p $HOME/bin
  export PATH=$PATH:$HOME/bin
  curl -o $HOME/bin/grab -L https://github.com/shellib/grab/raw/master/grab.sh && chmod +x $HOME/bin/grab
fi

source $(grab github.com/shellib/cli)
source $(grab github.com/shellib/maven as maven)

release_version=$(readopt --release-version $*)
if [ -z $release_version ]; then
  echo "Option --release_version is required!"
  exit 1
fi

function set_version() {
  local file=$1
  docker run -v `pwd`:/ws --workdir=/ws -i quay.io/jbangdev/jbang-action ./scripts/ChangeVersion.java ${file} io.dekorate $release_version > ${file}.versionChanged
  tail -n +2 ${file}.versionChanged > ${file} 
  rm ${file}.versionChanged
  git add ${file}
}

# Regenerate boms and set release version
mvn io.sundr:sundr-maven-plugin:0.92.1:generate-bom
cp -r target/classes/dekorate-bom/pom.xml boms/dekorate-bom/pom.xml
set_version boms/dekorate-bom/pom.xml
cp -r target/classes/dekorate-spring-bom/pom.xml boms/dekorate-spring-bom/pom.xml
set_version boms/dekorate-spring-bom/pom.xml

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
