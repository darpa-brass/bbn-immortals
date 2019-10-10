#!/usr/bin/env bash

set -e

mkdir -p external/challenge-problems/Scenarios/FlightTesting
cp -R "${IMMORTALS_CHALLENGE_PROBLEMS_ROOT}"/Scenarios/FlightTesting/Scenario_6 external/challenge-problems/Scenarios/FlightTesting/

mkdir -p external/immortals_root/knowledge-repo/cp/cp3.1/cp-ess-min/
cp -R ../../../knowledge-repo/cp/cp3.1/cp-ess-min/etc external/immortals_root/knowledge-repo/cp/cp3.1/cp-ess-min/
mkdir -p external/immortals_root/phase3/utils
cp -R ../bbn_test_scenarios external/immortals_root/phase3/utils/
mkdir -p external/immortals_root/phase3/immortals-orientdb-server/src/main/resources/
cp -R ../../immortals-orientdb-server/src/main/resources/*.json external/immortals_root/phase3/immortals-orientdb-server/src/main/resources/
mkdir -p external/immortals_root/knowledge-repo/cp/cp3.1/xsd-translation-service-test/target
cp ../../../knowledge-repo/cp/cp3.1/xsd-translation-service-test/target/immortals-xsd-translation-service-tester-boot.jar external/immortals_root/knowledge-repo/cp/cp3.1/xsd-translation-service-test/target/

mkdir xts_test_bundle
cp immortals_utils.py xts_test_bundle/
cp tester.py xts_test_bundle/
cp README.md xts_test_bundle/
cp __init__.py xts_test_bundle/
cp -R external xts_test_bundle/
tar cvzf xts_test_bundle.tar.gz xts_test_bundle
