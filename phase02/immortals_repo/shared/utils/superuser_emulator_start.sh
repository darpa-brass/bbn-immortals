#!/usr/bin/env bash

set -e

CURRENT_DEVICES=`adb devices`

if [[ "$CURRENT_DEVICES" != "List of devices attached" ]];then
  echo "Please halt any running emulators before starting one with this tool."
  exit
fi

EMULATOR_VERSION=21
EMULATOR_ARCH=x64

SUPERSU_APK="SuperSU-v2.79-20161205182033.apk"
SUPERSU_ZIP="SuperSU-v2.79-201612051815.zip"
SUPERSU_DL_URL="https://s3-us-west-2.amazonaws.com/supersu/download/zip/"
TMP_DIR=/tmp/mil.darpa.immortals/supersu/

# If no emulator identifier specified, exit
if [[ "${1}" == "" ]];then
  echo Please provide the name of the emulator you wish to create as a parameter!
  exit
fi

# Check if the avd has been created
AVD_LIST=(`emulator -list-avds`)
for element in ${AVD_LIST[@]};do
  if [[ "$element" == ${1} ]];then
    EMULATOR_NAME=${1}
  fi
done

# If not, create it
if [[ "${EMULATOR_NAME}" == "" ]];then
  echo CREATING AVD
  EMULATOR_NAME=${1}
  avdmanager create avd --package "system-images;android-21;default;x86_64" --device "Nexus 5X" --sdcard 100M --name "${EMULATOR_NAME}"
fi

# Start the emulator
echo STARTING EMULATOR
emulator -memory 3072 -cores 4 -netspeed gprs -selinux disabled -avd $EMULATOR_NAME -writable-system&
adb wait-for-device
sleep 10

# If SuperSU hasn't been installed, download and install it
#INSTALLED_PKG_LIST=`adb shell pm list packages`
if [[ `adb shell pm list packages | grep "^package:eu.chainfire.supersu.*$"` == "" ]];then
  FIRST_TIME=true
  echo INSTALLING SUPERSU

  if [ ! -d "${TMP_DIR}" ];then
    mkdir -p "${TMP_DIR}"
  fi

  if [ ! -f "${TMP_DIR}${SUPERSU_APK}" ];then
    wget ${SUPERSU_DL_URL}${SUPERSU_APK} --output-document ${TMP_DIR}${SUPERSU_APK}
  fi

  adb install "${TMP_DIR}${SUPERSU_APK}"
fi

if [[ "`adb shell ls /data/local/userinit.sh`" = *"No such file or directory"* ]];then
  FIRST_TIME=true
  echo INSTALLING SU

  if [ ! -d "${TMP_DIR}" ];then
    mkdir -p "${TMP_DIR}"
  fi
  if [ ! -f ${TMP_DIR}${EMULATOR_ARCH}/su ];then
    if [ ! -f "${TMP_DIR}${SUPERSU_ZIP}" ];then
      wget ${SUPERSU_DL_URL}${SUPERSU_ZIP} --output-document ${TMP_DIR}${SUPERSU_ZIP}
    fi
    unzip ${TMP_DIR}${SUPERSU_ZIP} -d ${TMP_DIR}
  fi

  adb root
  adb remount
  adb push ${TMP_DIR}${EMULATOR_ARCH}/su /system/xbin/su
  adb shell 'cd /system/xbin;chmod 06755 su'
  adb shell 'echo \#!/system/bin/sh > /data/local/userinit.sh'
  adb shell 'echo su -–install >> /data/local/userinit.sh'
  adb shell 'echo su -–daemon\& >> /data/local/userinit.sh'
  adb shell 'echo setenforce 0 >> /data/local/userinit.sh'
  adb shell 'chmod 777 /data/local/userinit.sh'
fi

echo STARTING SU
adb shell 'su --install;su --daemon&setenforce false'

if [[ "${FIRST_TIME}" != "" ]];then
  echo The emulator has been set up and started successfully. Please Open up the
  echo application menu in the emulator, Open the SuperSU application, select 
  echo Settings, and set Default access to \"Grant\".
fi

#echo The emulator has been set up successfullly. Please start it with the following command:
#echo   emulator -memory 3072 -cores 4 -netspeed gprs -selinux disabled -avd ${EMULATOR_NAME}
#echo It will not work if started with Android Studio due to it always defaulting to the untouched system partition.
