#!/usr/bin/env bash

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

# shellcheck source=opennms-container/horizon/config.sh
source ./config.sh

# shellcheck source=opennms-container/registry-config.sh
source ../registry-config.sh

# We don't want to push every build, just a floating bleeding image for latest snapshot builds
if [ "${CIRCLE_BRANCH}" == "develop " ]; then
  IMAGE_VERSION=("bleeding")
fi

# Relase branches get published versioned and with build number
for TAG in ${IMAGE_VERSION[*]}; do
  docker tag "${CONTAINER_PROJECT}" "${CONTAINER_REGISTRY}/${CONTAINER_REGISTRY_REPO}/${CONTAINER_PROJECT}:${TAG}"
  docker push "${CONTAINER_REGISTRY}/${CONTAINER_REGISTRY_REPO}/${CONTAINER_PROJECT}:${TAG}"
done
