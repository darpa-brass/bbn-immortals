#!/bin/bash


# export SESSION_ID="session-$(uuidgen)"
export SESSION_ID="I$(date +%s000)"

# This example generates a CP2 perturbation input file
# The session identifier is generated by the script.
python3 ./py/mission_perturb.py \
     --session ${SESSION_ID} \
     --output deployment_model.ttl \
     --template ./template/gme-template.ttl \
     --pli-client-msg-rate 10 \
     --image-client-msg-rate 1 \
     --server-bandwidth 2000 \
     --client-device-count 5 \
     --android-bluetooth-resource yes \
     --android-usb-resource yes \
     --android-internal-gps-resource yes \
     --android-ui-resource yes \
     --gps-satellite-resource yes \
     --mission-trusted-comms yes

# This shows how to post a perturbation file to the DAS-server
curl -H "Content-Type:text/plain" \
     -X POST \
     --data-binary @deployment_model.ttl \
     http://localhost:8080/bbn/das/deployment-model

# Run the simulation
python2 ../../harness/scenarioconductor/scenarioconductor.py \
    --sessionidentifier ${SESSION_ID} \
    --clientcount 5 \
    --serverbandwidth 2000 \
    --clientimagesendfrequency 1 \
    --clientmsgsendfrequency 10 \
    --android-bluetooth-resource true \
    --android-usb-resource true \
    --android-internal-gps-resource true \
    --android-ui-resource true \
    --gps-satellite-resource true \
    --mission-trusted-comms true

# Prepare and run subsequent tests
