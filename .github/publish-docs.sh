#!/bin/bash
version=$1;

yq eval ".release.current-version = \"$version\"" ../docs/_data/project.yml | sponge ../docs/_data/project.yml

