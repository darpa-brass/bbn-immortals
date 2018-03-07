#!/bin/sh
CLASS_DIR=$1
APK_DIR=$2
API_LEVEL=$3
BIN_DIR="$PWD/bin"
CURR_DIR=$PWD

RT="$PWD/sdk/platforms/default/rt.jar"
ASM="$PWD/soot/asm-all-5.0.4.jar"
SOOTCLASS="$PWD/soot/soot-trunk.jar"
POLYGLOT="$PWD/soot/polyglotclasses-1.3.5.jar"
JASMIN="$PWD/soot/jasminclasses-2.5.0.jar"
ANDROID="$PWD/sdk/platforms/default/android-24.jar"
OUTPUT="$PWD/output/$CLASS_DIR.output"

PROCESS_DIR="$APK_DIR/$CLASS_DIR"
DEX_FULLPATH="$APK_DIR/$APKNAME.dex"

#call this script with 
#./classAnalysis.sh class_file_dir class_file_dir_path api_level

# For example
#./classAnalysis.sh TakServerDataManager ~/yueduan/bbnAnalysis/apks 24


./sync_bin.sh

#echo $CURR_DIR
cd $BIN_DIR
#echo $CURR_DIR
echo "generating api graph for $CLASS_DIR... hopefully it's working"
java -Xss1024m -Xmx4096m -cp $ASM:$JASMIN:$SOOTCLASS:$POLYGLOT:$PROCESS_DIR:. mySoot.AnalyzerMain $PROCESS_DIR $DEX_FULLPATH $API_LEVEL > $OUTPUT &
