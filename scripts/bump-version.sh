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


git commit -m "chore(version bump) : version $1"
