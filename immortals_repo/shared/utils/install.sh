#!/usr/bin/env bash

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
REPO_DIR=${HOME}/immortals_repo

# If this file is within the repository, skp fetching
if [ -d "${SCRIPT_DIR}/../modules" ] && [ -d "${SCRIPT_DIR}/../../harness" ] && [ -d "${SCRIPT_DIR}/../../das/das-service" ];then
  echo Script is within an immortals root. Skipping repository pull.
  ARGS="${@:1}"
  cd ../../shared/utils
  
else
  echo Script is not within an immortals root. Pulling from repository.
  
  if [ "$2" == "--staging" ];then
    STAGING=true
    ARGS="${@:3}"
  else
    ARGS="${@:2}"
  fi
  # Otherwise, try and fetch
  if [ "$1" == "--bbn" ];then
    echo Checking out from BBN repository...
    svn co https://dsl-external.bbn.com/svn/immortals/trunk ${REPO_DIR}

  elif [ "$1" == "--mit" ];then
    echo Checkout out from MIT repository...
    git clone ssh://git@github.mit.edu/brass/bbn-immortals
    cd bbn-immortals

    if [ ${STAGING} ];then
      git checkout staging
    fi
    
    cp -R immortals_repo ${REPO_DIR}

  else
    echo Please specify the '--bbn' or '--mit' flag to indicate which repo to pull from!
    exit 1
  fi
  
  cd ${REPO_DIR}/shared/utils
fi

python3.5 install.py "$ARGS"
