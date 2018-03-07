#!/usr/bin/env bash

REPO_DIR=${HOME}/immortals_repo

set -e

cd ${REPO_DIR}/harness
./prepare_setup.sh --unattended-setup
. ./setup.sh
cp ${REPO_DIR}/harness/immortalsrc ${HOME}/.immortalsrc
