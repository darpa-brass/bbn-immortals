#!/usr/bin/env bash

set -e

CURRENT_DEVICES=`adb devices`

if [[ "$CURRENT_DEVICES" != "List of devices attached" ]];then
  echo "Please halt any running emulators before starting one with this tool."
  exit
fi

EMULATOR_VERSION=21
EMULATOR_ARCH=x64


# If no emulator identifier specified, exit
if [[ "${1}" == "" ]];then
  echo Please provide the name of the emulator you wish to create as a parameter!
  exit
fi


for emu_id in ${@};do
    # Check if the avd has been created
    AVD_LIST=(`emulator -list-avds`)
    for element in ${AVD_LIST[@]};do
      if [[ "$element" == ${emu_id} ]];then
        EMULATOR_NAME=${emu_id}
      fi
    done

    # If not, create it
    if [[ "${EMULATOR_NAME}" == "" ]];then
      echo CREATING AVD
      EMULATOR_NAME=${emu_id}
      avdmanager create avd --package "system-images;android-21;default;x86_64" --device "Nexus 5X" --sdcard 100M --name "${EMULATOR_NAME}"
    fi

    # Start the emulator
    echo STARTING EMULATOR
    emulator -memory 3072 -cores 4 -netspeed gprs -selinux disabled -avd $EMULATOR_NAME&
    adb wait-for-device
    unset EMULATOR_NAME
done

sleep 10
