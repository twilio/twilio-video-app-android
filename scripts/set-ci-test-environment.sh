#!/bin/bash

# Set default test environment to stage
TEST_ENVIRONMENT=stage
GIT_COMMIT_MSG=$(git log --format="%B" -n 1 $CIRCLE_SHA1 | xargs)
{ echo $GIT_COMMIT_MSG | grep "\[env:stage\]" ; } && TEST_ENVIRONMENT=stage
{ echo $GIT_COMMIT_MSG | grep "\[env:prod\]" ; } && TEST_ENVIRONMENT=prod

# Set the test environment branch to prod for master, feature, or the default branch regardless
# of the commit message
if [[ "$CIRCLE_BRANCH" == "master" || "$CIRCLE_BRANCH" == "feature/"* ]]; then
  TEST_ENVIRONMENT=prod
fi

echo "Test Environment: $TEST_ENVIRONMENT"

# Set the local.properties
echo "ENVIRONMENT=$TEST_ENVIRONMENT" >> local.properties