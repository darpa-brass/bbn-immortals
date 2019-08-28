#!/usr/bin/env bash

set -e

cd ../../../dsl/resource-dsl/

DT='../../phase3/utils/dsltest/'
RULES="${DT}swap-rules.json"


echo RUNNING RP0-IP0
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}" --inventory-file "${DT}inventory-IP0.json" --request-file "${DT}input-RP0.json"

echo RUNNING RP0-IP1
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}inventory-IP1.json" --request-file "${DT}input-RP0.json"

echo RUNNING RP0-IP0IP1
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}" --inventory-file "${DT}inventory-IP0IP1.json" --request-file "${DT}input-RP0.json"

echo RUNNING RP1-IP0
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}inventory-IP0.json" --request-file "${DT}input-RP1.json"

echo RUNNING RP1-IP1
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}inventory-IP1.json" --request-file "${DT}input-RP1.json"

echo RUNNING RP1-IP1
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}inventory-IP0IP1.json" --request-file "${DT}input-RP1.json"

echo RUNNING RP0RP1-IP0IP1-twoInventoryDaus
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}inventory-IP0IP1-twoDaus.json" --request-file "${DT}input-RP0RP1.json"

echo RUNNING RP0RP1-IP0IP1-singleInventoryDau
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}inventory-IP0IP1.json" --request-file "${DT}input-RP0RP1.json"

echo RUNNING DSLInterchangeFormat Test
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}DSLInterchangeFormat-dsl-dauinventory.json" --request-file "${DT}DSLInterchangeFormat-dsl-input.json"

echo RUNNING SwRI Example 1 Test
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}s5e1-inventory.json" --request-file "${DT}s5e1-request.json"

echo RUNNING SwRI Example 2 Test - Bus Ports
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}/s5e2/dsl-swap-inventory.json" --request-file "${DT}/s5e2/dsl-swap-request-bus.json"

echo RUNNING SwRI Example 2 Test - No Signal Conditioner Ports
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}/s5e2/dsl-swap-inventory.json" --request-file "${DT}/s5e2/dsl-swap-request-nosignalconditioners.json"

echo RUNNING SwRI Example 2 Test - Signal Conditioner Ports
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}/s5e2/dsl-swap-inventory.json" --request-file "${DT}/s5e2/dsl-swap-request-signalconditioners.json"
echo RUNNING SwRI Example 2 Test
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}/s5e2/dsl-swap-inventory.json" --request-file "${DT}/s5e2/dsl-swap-request.json"
