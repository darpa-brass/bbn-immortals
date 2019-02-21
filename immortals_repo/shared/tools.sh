#!/usr/bin/env bash

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
    if [[ "$1" != "installer" ]];then
        echo "No immortalsrc found! If your primary environment is not configured to run IMMoRTALS you will likely have issues!"
    fi
else
    source "$IMMORTALSRC_PATH"
fi

SCRIPT_PARAMS=1

if [ "$1" == "--cp-profile" ];then
    export IMMORTALS_CP_PROFILE=${2}
    SCRIPT_PARAMS=$(( 3 > $SCRIPT_PARAMS ? 3 : $SCRIPT_PARAMS ))
elif [ "$3" == "--cp-profile" ];then
    export IMMORTALS_CP_PROFILE=${4}
    SCRIPT_PARAMS=$(( 5 > $SCRIPT_PARAMS ? 5 : $SCRIPT_PARAMS ))
elif [ "$5" == "--cp-profile" ];then
    export IMMORTALS_CP_PROFILE=${6}
    SCRIPT_PARAMS=$(( 7 > $SCRIPT_PARAMS ? 7 : $SCRIPT_PARAMS ))
fi


if [ "$1" == "--env-profile" ];then
    export IMMORTALS_ENV_PROFILE=${2}
    SCRIPT_PARAMS=$(( 3 > $SCRIPT_PARAMS ? 3 : $SCRIPT_PARAMS ))
elif [ "$3" == "--env-profile" ];then
    export IMMORTALS_ENV_PROFILE=${4}
    SCRIPT_PARAMS=$(( 5 > $SCRIPT_PARAMS ? 5 : $SCRIPT_PARAMS ))
elif [ "$5" == "--env-profile" ];then
    export IMMORTALS_ENV_PROFILE=${6}
    SCRIPT_PARAMS=$(( 7 > $SCRIPT_PARAMS ? 7 : $SCRIPT_PARAMS ))
fi


if [ "$1" == "--set-override-file-data" ];then
    OVERRIDE_TARGET_DATA=$2
    SCRIPT_PARAMS=$(( 3 > $SCRIPT_PARAMS ? 3 : $SCRIPT_PARAMS ))
elif [ "$3" == "--set-override-file-data" ];then
    OVERRIDE_TARGET_DATA=$4
    SCRIPT_PARAMS=$(( 5 > $SCRIPT_PARAMS ? 5 : $SCRIPT_PARAMS ))
elif [ "$5" == "--set-override-file-data" ];then
    OVERRIDE_TARGET_DATA=$6
    SCRIPT_PARAMS=$(( 7 > $SCRIPT_PARAMS ? 7 : $SCRIPT_PARAMS ))
fi

if [ "${OVERRIDE_TARGET_DATA}" != "" ];then
    OVERRIDE_TARGET_FILE="${HOME}/IMMORTALS_OVERRIDE_FILE.json"
    echo "${OVERRIDE_TARGET_DATA}" > "${OVERRIDE_TARGET_FILE}"
    export IMMORTALS_OVERRIDE_FILE="${OVERRIDE_TARGET_FILE}"
elif [ -f "${HOME}/immortals_override_file.json" ];then
    export IMMORTALS_OVERRIDE_FILE=${HOME}/immortals_override_file.json
fi

python3 ${SCRIPT_DIR}tools/tools.py "${@:$SCRIPT_PARAMS}"
