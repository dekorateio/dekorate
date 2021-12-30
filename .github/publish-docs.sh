#!/bin/bash
version=$1;

yq eval ".release.current-version = \"$version\"" docs/_data/project.yml | sponge docs/_data/project.yml
git add docs/_data/project.yml
git commit -m "chore: updata project version for Dekorate docs."
git push auri -u jekyll-site

