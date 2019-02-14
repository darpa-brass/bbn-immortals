#!/bin/sh
APKNAME=$1
CLASS_DIR=$2
APK_DIR=$3
APP_EXT=$4
API_LEVEL=$5
TRANSBLOCKER="$PWD/bin"
CURR_DIR=$PWD

SOOTCLASS="$PWD/soot/soot-trunk.jar"
POLYGLOT="$PWD/soot/polyglotclasses-1.3.5.jar"
JASMIN="$PWD/soot/jasminclasses-2.5.0.jar"
ASM="$PWD/soot/asm-all-5.0.4.jar"
ONTOLOGY="$PWD/soot/immortals-ontology-pojo-api-r2.0.0.jar"
ANDROID="$PWD/sdk/platforms/default/android-24.jar"
OUTPUT="$PWD/output/$CLASS_DIR.output"
GXL_DIR="$PWD/output/gxl_file"
TEMP_PID="$PWD/tempPid"

PROCESS_DIR="$APK_DIR/$CLASS_DIR"
DEX_FULLPATH="$APK_DIR/$APKNAME.dex"

#call this script with 
# ./resourceAnalysis.sh $file ${file}dir $INPUT_DIR $APKEXT ${files[$i]}
# For example
# ./resourceAnalysis.sh BluetoothSimulatedGpsRunner BluetoothSimulatedGpsRunnerdir /home/yduan/yueduan/bbnAnalysis/apks .apk 24 


#if [ ! -d "$GXL_DIR" ]; then
#        mkdir $GXL_DIR
#fi

./sync_bin.sh
echo converting Dalvik executable to Java classes...
./prepare.sh $APKNAME $CLASS_DIR $APK_DIR $APP_EXT

#echo $CURR_DIR
cd $TRANSBLOCKER
#echo $CURR_DIR
echo "Analyzing $CLASS_DIR... hopefully it's working"
java -Xss1024m -Xmx4096m -cp $ONTOLOGY:$JASMIN:$SOOTCLASS:$POLYGLOT:$ANDROID:$ASM:$PROCESS_DIR:. mySoot.AnalyzerMain $PROCESS_DIR $DEX_FULLPATH $API_LEVEL > $OUTPUT &
