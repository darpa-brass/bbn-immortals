#!/usr/bin/env bash

set -e

IMMORTALSRC_PATH=""

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

elif [ -f "../.immortalsrc" ];then
    echo ".immortalsrc found in source root. Sourcing..."
    IMMORTALSRC_PATH="../.immortalsrc"

elif [ -f "../immortalsrc" ];then
    echo "immortalsrc found in source root. Sourcing..."
    IMMORTALSRC_PATH="../immortalsrc"
fi

if [ "$IMMORTALSRC_PATH" == "" ];then
    echo "No immortalsrc found! If your primary environment is not configured to run IMMoRTALS you will likely have issues!"
else
    source "$IMMORTALSRC_PATH"
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export IMMORTALS_OVERRIDE_FILE="${DIR}/sample_override_file.json"

python3.5 tools.py orchestrate cp1
