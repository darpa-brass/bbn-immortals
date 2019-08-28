#!/usr/bin/env bash

# Sample Start: ./start.sh  --odb-url remote:127.0.0.1:2424/IMMORTALS_TEST-SCENARIO_5 --odb-user admin --odb-password admin --persistence-url remote:127.0.0.1:2424/BBNPersistent --scenario 5

set -e

IMMORTALSRC_PATH=""

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )/"

if [ "$IMMORTALSRC" != "" ];then
    if [ -f "$IMMORTALSRC" ];then
        echo "Environment variable IMMORTALSRC points to a valid file. Sourcing..."
        IMMORTALSRC_PATH="$IMMORTALSRC"
    else
        echo "IMMORTALSRC environment variable defined as ${IMMORTALSRC}, but the file does not exist!"
    fi

elif [ -f "${HOME}/.immortalsrc" ];then
    echo ".immortalsrc found in home directory. Sourcing..."
    IMMORTALSRC_PATH="${HOME}/.immortalsrc"

elif [ -f "${HOME}/immortalsrc" ];then
    echo "immortalsrc found in home directory. Sourcing..."
    IMMORTALSRC_PATH="${HOME}/immortalsrc"

elif [ -f "${SCRIPT_DIR}../.immortalsrc" ];then
    echo ".immortalsrc found in source root. Sourcing..."
    IMMORTALSRC_PATH="${SCRIPT_DIR}../.immortalsrc"

elif [ -f "${SCRIPT_DIR}../immortalsrc" ];then
    echo "immortalsrc found in source root. Sourcing..."
    IMMORTALSRC_PATH="${SCRIPT_DIR}../immortalsrc"
fi

if [ "$IMMORTALSRC_PATH" == "" ];then
    echo "No immortalsrc found! If your primary environment is not configured to run IMMoRTALS you will likely have issues!"
else
    source "$IMMORTALSRC_PATH"
fi

if [ -d "${HOME}/.immortals/anaconda" ];then
    _CONDA_ROOT="${HOME}/.immortals/anaconda"
    \. "$_CONDA_ROOT/etc/profile.d/conda.sh" || return $?
    _conda_activate

    if [ -d "${HOME}/.immortals/anaconda/envs/aql" ]; then
        conda activate aql
    fi
fi

python3 ${SCRIPT_DIR}start.py $@
