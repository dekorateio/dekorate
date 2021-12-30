#!/bin/bash
version=$1;
echo "--- estoy aqui: $pwd"
ls -lisa
yq eval ".release.current-version = \"$version\"" docs/_data/project.yml | sponge docs/_data/project.yml

