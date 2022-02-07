#!/bin/bash

release_version=$1;
if [ -z $release_version ]; then
  echo "Option --release_version is required!"
  exit 1
fi

echo "Setting release_version=$release_version"
yq eval ".release.current-version = \"$release_version\"" docs/_data/project.yml | sponge docs/_data/project.yml

