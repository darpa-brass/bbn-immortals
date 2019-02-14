#! /bin/sh

SCRIPT_DIR=`cd "$(dirname "$0")"; pwd`
JAR_PATH=${SCRIPT_DIR}/lib/pql_on_android.jar

echo $SCRIPT_DIR

SOURCE_TO_MODIFY="${1}"
SOURCE_TO_ANALYZE="${2}"
LOCATION_ANDROID_JAR="${3}"

# Run PQL
echo "Running PQL..."
java -jar ${JAR_PATH} ${SOURCE_TO_MODIFY} ${SOURCE_TO_ANALYZE} ${LOCATION_ANDROID_JAR}
