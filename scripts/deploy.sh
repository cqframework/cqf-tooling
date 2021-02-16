#!/usr/bin/env bash

# Fail if any command fails or if there are unbound variables and check syntax
set -euxo pipefail
bash -n "$0"

if [[ "$TRAVIS_BRANCH" != "master" || ! -z "$TRAVIS_TAG" ]]
then
  echo "We're not on the master branch or a git tag. Skipping deploy."
  exit 0
fi

# Import maven settings
cp .travis.settings.xml $HOME/.m2/settings.xml

CMD="mvn deploy -DskipTests=true -Dmaven.test.skip=true -T 4 -B -P ci"

# Import signing key and publish a release on a tag
# TODO: Make sure the tag value matches the mvn version
if [[ ! -z "$TRAVIS_TAG" ]]
then
    echo "Publishing Maven Central release for tag: $TRAVIS_TAG"
    echo $GPG_SECRET_KEYS | base64 --decode| $GPG_EXECUTABLE --import;
    echo $GPG_OWNERTRUST | base64 --decode | $GPG_EXECUTABLE --import-ownertrust;
    # Activate the release profile
    CMD="$CMD,release"
else
   echo "Publishing Maven Central snapshot / pre-release for branch: $TRAVIS_BRANCH"
fi 

eval $CMD