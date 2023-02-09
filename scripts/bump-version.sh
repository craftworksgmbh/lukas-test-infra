#!/usr/bin/env bash

# Set working dir
cd "$(dirname "$0")/.."

# exit when any command fails
set -e

VERSION=$1
SEMVER_REGEX="^(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)$"

validate_version() {
  if [[ "$VERSION" =~ $SEMVER_REGEX ]]; then
    echo "version $VERSION"
  else
    echo "version $VERSION does not match the semver scheme 'X.Y.Z'."
    exit 255
  fi
}

# ===marker:start:python===
if ! [ -x "$(command -v tbump)" ]; then
    echo '\033[1;31mError: tbump is not installed. Install it. E.g. via `pip install tbump`\033[0m' >&2
    exit 1
fi
# ===marker:end:python===

validate_version

echo "This script will set a new version."
echo "To deploy - push to dev, create a PR and merge into master"

mvn versions:set -DnewVersion=$1
mvn versions:commit
mvn validate -P version-set

git add pom.xml
git add ./**/pom.xml
git add ./**/package-lock.json
git add ./**/package.json

# ===marker:start:python===
# If there are other file that contain the version they need to be manually added here
git add ./__nameKebab__-python/src/__projectLower__/__init__.py
# ===marker:end:python===

git commit -m "chore(version bump) : version $1"
