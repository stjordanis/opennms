#!/usr/bin/env bash

# shellcheck disable=SC2034

# Relative path to helper build helper scripts
SCRIPTS_PATH="../../.circleci/scripts"

# Base Image Dependency
BASE_IMAGE="opennms/openjdk"
BASE_IMAGE_VERSION="1.8.0.201.b09-b1"
BUILD_DATE="$(date -u +"%Y-%m-%dT%H:%M:%S%z")"

# Minion Image versioning
VERSION=$("${SCRIPTS_PATH}"/version-from-pom.py ../../pom.xml)

# Use version number for OCI tags
IMAGE_VERSION=("${VERSION}")

# Most specific tag when it is not build locally and in CircleCI
if [ -n "${CIRCLE_BUILD_NUM}" ]; then
  IMAGE_VERSION+=("${VERSION}-cb.${CIRCLE_BUILD_NUM}")
fi

REPO_HOST="yum.opennms.org"
REPO_RELEASE="stable"
REPO_RPM="https://${REPO_HOST}/repofiles/opennms-repo-${REPO_RELEASE}-rhel7.noarch.rpm"
REPO_KEY_URL="https://${REPO_HOST}/OPENNMS-GPG-KEY"

# System Package dependencies
PACKAGES="wget
          gettext
          jicmp
          jicmp6"

#
# If you want to install packages from the official repository, add your packages here.
# By default the build system will build the RPMS in the ./rpms directory and install from here.
#
# Suggested packages to install OpenNMS Minion packages from repository
MINION_PACKAGES="opennms-minion-container
                 opennms-minion-features-core
                 opennms-minion-features-default"

# Run as user
USER="minion"
GROUP="minion"
