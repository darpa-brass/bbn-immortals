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
    echo "No immortalsrc found! If your primary environment is not configured to run IMMoRTALS you will likely have issues!"
else
    source "$IMMORTALSRC_PATH"
fi

if [[ "$1" == "" ]];then
    DO_SCENARIO_5=true
    DO_SCENARIO_6=true

elif [[ "$1" == "--scenario" ]];then
    if [[ "$2" == "5" ]];then
        DO_SCENARIO_5=true
        DO_SCENARIO_6=false
        shift
        shift

    elif [[ "$2" == "6" ]];then
        DO_SCENARIO_5=false
        DO_SCENARIO_6=true
        shift
        shift

    elif [[ "$2" == "all" ]];then
        DO_SCENARIO_5=false
        DO_SCENARIO_6=true
        shift
        shift

    else
        echo Invalid scenario value \"$2\"! Valid values: [\"5\", \"6\", \"all\"]
        exit 1
    fi

else
    echo Invalid parameter \"$1\"! Valid values: [\"--scenario\"]
    exit 1

fi


if [[ ${DO_SCENARIO_5} == true ]];then
    echo Building Scenario 5!

    cd mdl-schema-evolution/
    ./gradlew clean build publishImmortalsMseLibPublicationToMavenLocal publishMseLibPublicationToMavenLocal
    cd ../

    cd flighttest-constraint-solver/
    ./gradlew clean build
    cd ../

    cd ../dsl/resource-dsl/
    stack setup
    stack build

    stack exec resource-dsl -- swap-dau --init
    cd ../../phase3
fi

if [[ ${DO_SCENARIO_6} == true ]];then
    echo Building Scenario 6!

    source ~/.immortals/anaconda/bin/activate
    cd ../knowledge-repo/cp/cp3.1/xsd-tranlsation-service-aql/aql/
    if [ ! -d "${HOME}/.immortals/anaconda/envs/aql" ]; then
        conda env create -f environment.yml
    fi
    cd ../../../../../phase3

    cd mdl-schema-evolution/
    ./gradlew clean build publishImmortalsMseLibPublicationToMavenLocal publishMseLibPublicationToMavenLocal
    cd ../

    # Build Securboration artifacts and return to the immortals root
    exec mvn -f ../knowledge-repo/pom.xml clean install -DskipTests
    exec mvn -f ../knowledge-repo/cp/cp3.1/xsd-translation-service/pom.xml clean install -DskipTests
fi

