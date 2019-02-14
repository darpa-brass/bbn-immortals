#!/usr/bin/env bash

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

elif [ -f "../.immortalsrc" ];then
    echo "immortalsrc found in source root. Sourcing..."
    IMMORTALSRC_PATH="../.immortalsrc"
fi

if [ "$IMMORTALSRC_PATH" == "" ];then
    echo "No existing immortalsrc found. Starting from scratch..."
else
    source "$IMMORTALSRC_PATH"
fi

python3 -m installer.prepare "$@"
