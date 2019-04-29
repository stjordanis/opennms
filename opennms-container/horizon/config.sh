#!/usr/bin/env bash

# shellcheck disable=SC2034

# Relative path to helper build helper scripts
SCRIPTS_PATH="../../.circleci/scripts"

# Base Image Dependency
BASE_IMAGE="opennms/horizon-base"
BASE_IMAGE_VERSION="1.0.0"
BUILD_DATE="$(date -u +"%Y-%m-%dT%H:%M:%S%z")"

# Horizon Image versioning
VERSION=$("${SCRIPTS_PATH}"/version-from-pom.py ../../pom.xml)

# Docker Tags, use version by default
IMAGE_VERSION=("${VERSION}")

# Add Docker tag when build in CircleCI with build number
if [ -n "${CIRCLE_BUILD_NUM}" ]; then
  IMAGE_VERSION+=("${VERSION}-cb.${CIRCLE_BUILD_NUM}")
fi

# If you want to install packages from the official repository, add your packages here.
# By default the build system will build the RPMS in the ./rpms directory and install from here.
#
# Suggested packages to install OpenNMS Horizon packages from repository
#
ONMS_PACKAGES="opennms-core
               opennms-webapp-jetty
               opennms-webapp-hawtio"
