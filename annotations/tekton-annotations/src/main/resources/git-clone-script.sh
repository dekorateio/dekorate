#!/usr/bin/env sh
set -eu
CHECKOUT_DIR="$(workspaces.source.path)"

git config --global --add safe.directory "$CHECKOUT_DIR"
/ko-app/git-init \
  -url="${PARAM_URL}" \
  -revision="${PARAM_REVISION}" \
  -path="${CHECKOUT_DIR}"

cd "${CHECKOUT_DIR}"
RESULT_SHA="$(git rev-parse HEAD)"
EXIT_CODE="$?"
if [ "${EXIT_CODE}" != 0 ] ; then
  exit "${EXIT_CODE}"
fi
