#!/usr/bin/env bash

set -e

cd ../../../dsl/resource-dsl/

DT='../../phase3/utils/dsltest/'
RULES="${DT}swap-rules.json"

echo RUNNING SwRI Example 2 Test - Bus Ports
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}/s5e2/dsl-swap-inventory.json" --request-file "${DT}/s5e2/dsl-swap-request-bus.json"

echo RUNNING SwRI Example 2 Test - No Signal Conditioner Ports
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}/s5e2/dsl-swap-inventory.json" --request-file "${DT}/s5e2/dsl-swap-request-nosignalconditioners.json"

echo RUNNING SwRI Example 2 Test - Signal Conditioner Ports
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}/s5e2/dsl-swap-inventory.json" --request-file "${DT}/s5e2/dsl-swap-request-signalconditioners.json"
echo RUNNING SwRI Example 2 Test
stack exec resource-dsl -- swap-dau --run --rules-file "${RULES}"  --inventory-file "${DT}/s5e2/dsl-swap-inventory.json" --request-file "${DT}/s5e2/dsl-swap-request.json"
