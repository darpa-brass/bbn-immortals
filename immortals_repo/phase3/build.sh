#!/usr/bin/env bash

set -e

IMMORTALSRC_PATH=""

PPWD="`pwd`"
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )/"

ARTIFACT_NAME=immortals-exe
ARTIFACT_ROOT="${SCRIPT_DIR}/${ARTIFACT_NAME}"

if [ "${IMMORTALSRC}" != "" ];then
    if [ -f "${IMMORTALSRC}" ];then
        echo "Environment variable IMMORTALSRC points to a valid file. Sourcing..."
        IMMORTALSRC_PATH="${IMMORTALSRC}"
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

if [ "${IMMORTALSRC_PATH}" == "" ];then
    echo "No immortalsrc found! If your primary environment is not configured to run IMMoRTALS you will likely have issues!"
else
    source "${IMMORTALSRC_PATH}"
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

if [[ -d "$ARTIFACT_ROOT" ]];then
    rm -r "$ARTIFACT_ROOT"
fi

mkdir "$ARTIFACT_ROOT"

if [[ ${DO_SCENARIO_5} == true ]];then
    echo Building Scenario 5!

    "${SCRIPT_DIR}/mdl-schema-evolution/gradlew" --no-daemon --build-file "${SCRIPT_DIR}/mdl-schema-evolution/build.gradle" clean build publishImmortalsMseLibPublicationToMavenLocal publishMseLibPublicationToMavenLocal

    "${SCRIPT_DIR}/flighttest-constraint-solver/gradlew" --no-daemon --build-file "${SCRIPT_DIR}/flighttest-constraint-solver/build.gradle" clean build

    cd "${SCRIPT_DIR}/../dsl/resource-dsl/"
    stack setup
    stack build
    stack exec resource-dsl -- swap-dau --init
    cd "${PPWD}"

    mkdir "${ARTIFACT_ROOT}/phase3"
    mkdir "${ARTIFACT_ROOT}/phase3/flighttest-constraint-solver"

    cp "${SCRIPT_DIR}/flighttest-constraint-solver/flighttest-constraint-solver.jar" "${ARTIFACT_ROOT}/phase3/flighttest-constraint-solver/"
    cp "${SCRIPT_DIR}/start.py" "${ARTIFACT_ROOT}/phase3/"
    cp "${SCRIPT_DIR}/start.sh" "${ARTIFACT_ROOT}/phase3/"

    cp -R "${SCRIPT_DIR}/../dsl" "${ARTIFACT_ROOT}/"
fi

if [[ ${DO_SCENARIO_6} == true ]];then
    echo Building Scenario 6!

    cd "${SCRIPT_DIR}/../knowledge-repo/cp/cp3.1/xsd-tranlsation-service-aql/aql/"
    source ~/.immortals/anaconda/bin/activate
    if [ ! -d "${HOME}/.immortals/anaconda/envs/aql" ]; then
        conda env create -f environment.yml
    fi
    cd "${PPWD}"

    "${SCRIPT_DIR}/mdl-schema-evolution/gradlew" --no-daemon --build-file "${SCRIPT_DIR}/mdl-schema-evolution/build.gradle" clean build publishImmortalsMseLibPublicationToMavenLocal publishMseLibPublicationToMavenLocal

    # Build Securboration artifacts and return to the immortals root
    mvn -f "${SCRIPT_DIR}/../knowledge-repo/pom.xml" clean install -DskipTests
    mvn -f "${SCRIPT_DIR}/../knowledge-repo/cp/cp3.1/xsd-translation-service/pom.xml" clean install -DskipTests

    cp -R "${SCRIPT_DIR}/../knowledge-repo" "${ARTIFACT_ROOT}/"
fi

mkdir -p "${ARTIFACT_ROOT}/shared"
cp -R "${SCRIPT_DIR}/../shared/tools" "${ARTIFACT_ROOT}/shared"
cp -R "${SCRIPT_DIR}/../shared/tools.sh" "${ARTIFACT_ROOT}/shared/tools.sh"


if [[ -f "${ARTIFACT_ROOT}.tar.gz" ]];then
    rm "${ARTIFACT_ROOT}.tar.gz"
fi

cd "${SCRIPT_DIR}"
tar cvzf "${ARTIFACT_ROOT}.tar.gz" ${ARTIFACT_NAME}
cd "${PPWD}"
