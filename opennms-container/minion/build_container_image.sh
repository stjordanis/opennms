#!/usr/bin/env bash

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

# shellcheck source=minion/config.sh
source ./config.sh

# shellcheck source=registry-config.sh
source ../registry-config.sh

docker build -t minion \
  --build-arg BUILD_DATE="${BUILD_DATE}" \
  --build-arg BASE_IMAGE="${BASE_IMAGE}" \
  --build-arg BASE_IMAGE_VERSION="${BASE_IMAGE_VERSION}" \
  --build-arg VERSION="${VERSION}" \
  --build-arg MINION_PACKAGES="${MINION_PACKAGES}" \
  .

docker image save minion -o images/container.oci
