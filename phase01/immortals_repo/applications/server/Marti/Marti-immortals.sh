#!/usr/bin/env bash

DO_START=0
DO_STOP=0
DO_DAEMON=0
DO_STATUS=0

DO_START_INVALIDS=("--stop" "--status")
DO_STOP_INVALIDS=("--start" "--status" "--daemon")
DO_DAEMON_INVALIDS=("--stop" "--status")
DO_STATUS_INVALIDS=("--start" "--stop" "--daemon")

DO_DAEMON_REQUIREMENTS=("--start")

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Used to prevent possible PID lookup conflicts between instances run through this script and other execution methods
MARTI_PATH="${SCRIPT_DIR}/Marti-immortals.jar"
TMP_MARTI_PATH="${SCRIPT_DIR}/.Marti-immortals-sh-do_not_run_directly.jar"
PID_FILE="/tmp/Marti-immortals-sh-do_not_run_directly.pid"

echo $MARTI_PATH

function displayHelp() {
  echo "Usage Instructions:"
  echo "  This script is used to start the Immortals Marti variant. usage parameters:"
  echo "    --start    Starts the server"
  echo "    --stop     Stops the server"
  echo "    --daemon   Starts the server in Daemon mode"
  echo "    --status   Checks the server's current status"
}


if [ $# -eq 0 ];then
  displayHelp
  exit
fi

for param in "$@"; do
  FOUND=0

  for idx in `seq 0 $(expr ${#} + 1)`; do

    if [ "$param" = "--start" ];then
      for invalidOption in "${DO_START_INVALIDS[@]}";do
        if [[ "$*" == *"$invalidOption"* ]];then
          echo "Cannot use paramter '${invalidOption}' with parameter '${param}'!"
          echo
          displayHelp
          exit
        fi
      done
      DO_START=1

    elif [ "$param" = "--stop" ];then
      for invalidOption in "${DO_STOP_INVALIDS[@]}";do
        if [[ "$*" == *"$invalidOption"* ]];then
          echo "Cannot use paramter '${invalidOption}' with parameter '${param}'!"
          echo
          displayHelp
          exit
        fi
      done
      DO_STOP=1

    elif [ "$param" = "--daemon" ];then
      for invalidOption in "${DO_DAEMON_INVALIDS[@]}";do
        if [[ "$*" == *"$invalidOption"* ]];then
          echo "Cannot use paramter '${invalidOption}' with parameter '${param}'!"
          echo
          displayHelp
          exit
        fi
      done

      for requirement in "${DO_DAEMON_REQUIREMENTS[@]}";do
        if [[ "$*" != *"$requirement"* ]];then
          echo "Cannot use paramter '${param}' without parameter '${requirement}'!"
          echo
          displayHelp
          exit
        fi
      done
      DO_DAEMON=1;

    elif [ "$param" = "--status" ];then
      for invalidOption in "${DO_STATUS_INVALIDS[@]}";do
        if [[ "$*" == *"$invalidOption"* ]];then
          echo "Cannot use paramter '${invalidOption}' with parameter '${param}'!"
          echo
          displayHelp
          exit
        fi
      done
      DO_STATUS=1

    elif [ "$param" = "--help" ];then
      displayHelp
      exit

    else
      echo "Unexpected parameter '${param}'!"
      echo
      displayHelp
      exit
    fi

  done

done

if [ "$DO_START" -eq 1 ];then
  # Renaming/copying the binary seems like the easiest way to prevent PID conflicts with servers not run through this script
  if [ "$DO_DAEMON" -eq 1 ]; then
    cp "${MARTI_PATH}" "${TMP_MARTI_PATH}"
    java -jar "${TMP_MARTI_PATH}" &
    echo `ps -ef | grep "java -jar ${TMP_MARTI_PATH}" | grep -v grep | awk '{print $2}'` > "$PID_FILE"
  else
    java -jar "${MARTI_PATH}"
  fi


elif [ "$DO_STOP" -eq 1 ];then
  kill `cat ${PID_FILE}`
  rm ${PID_FILE}
  rm ${TMP_MARTI_PATH}

elif [ "$DO_STATUS" -eq 1 ];then
  TRACKED_PID=-1

  if [ -e "$PID_FILE" ];then
    TRACKED_PID=`cat ${PID_FILE}`
  fi

  CURRENT_PID=`ps -ef | grep "java -jar ${TMP_MARTI_PATH}" | grep -v grep | awk '{print $2}'`

  if [ "$TRACKED_PID" == "$CURRENT_PID" ];then
    echo "Server associated with this script is currently running."

  elif [ "$TRACKED_PID" = "-1" ];then
    if [ "$CURRENT_PID" = "" ];then
      echo "No server is currently running."

    else
      echo "A server is currently running. However, it is not associated with this script."
    fi

  else
    if [ "$CURRENT_PID" = "" ];then
      echo "No server is currently running"
    else
      echo "One or more currently running servers are not associated with this script."
    fi
  fi
fi
