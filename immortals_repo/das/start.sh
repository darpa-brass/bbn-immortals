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

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

exec ${JAVA_HOME}/bin/java -jar ${SCRIPT_DIR}/das-launcher-2.0-LOCAL.jar "$@"
