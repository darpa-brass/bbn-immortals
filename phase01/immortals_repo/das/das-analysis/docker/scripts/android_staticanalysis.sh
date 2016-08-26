#!/usr/bin/env bash

FILE=${1}

EXTENSION="${FILE##*.}"
FILENAME="${FILE%.*}"

cd /bbnAnalysis

echo ./api-graph.sh ${FILENAME} ${FILENAME}dir /bbnAnalysis/apks .${EXTENSION} 23
bash api-graph.sh ${FILENAME} ${FILENAME}dir /bbnAnalysis/apks .${EXTENSION} 23
