#!/usr/bin/env bash

TGT=debugArchive
SRC=`realpath "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )/../../"`

#SRC=immortals_repo

declare -a backupPaths=(
"phase3/flighttest-constraint-solver/build/reports"
"phase3/flighttest-constraint-solver/build/test-results"
"phase3/flighttest-constraint-solver/build/tmp/test"

"phase3/immortals-orientdb-server/build/reports"
"phase3/immortals-orientdb-server/build/test-results"

"phase3/integration-tests/build/reports"
"phase3/integration-tests/build/test-results"
"phase3/integration-tests/build/tmp/integrationTest"
)

declare -a backupExcludePaths=(
"phase3/integration-tests/build/tmp/integrationTest/databases"
"phase3/flighttest-constraint-solver/build/tmp/test/databases"
"phase3/flighttest-constraint-solver/build/tmp/test/databases"

)


if [[ -d "${TGT}" ]];then
    rm -R "${TGT}"
fi


for path in ${backupPaths[@]};do
    mkdir -p "${TGT}/${path}"
    PATH_TGT=`realpath "${TGT}/${path}/../"`
    cp -R "${SRC}/${path}" "${PATH_TGT}/"
done

for path in ${backupExcludePaths[@]};do
    TGT_PATH="${TGT}/${path}"
    if [[ -e "${TGT_PATH}" ]];then
        TGT_PATH=`realpath "${TGT_PATH}"`
        rm -r "${TGT_PATH}"
    fi
done

tar cvzf "${TGT}.tar.gz" "${TGT}"