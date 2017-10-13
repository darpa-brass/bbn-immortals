OUTPUT_FOLDER="out"

mkdir $OUTPUT_FOLDER


java -jar ../../Castor/Castor.jar -parameters castor-input/parameters.json -inds castor-input/inds-schema4.json -dataModel castor-input/datamodel-schema4.json -trainPosSuffix "_TRAIN_POS" -trainNegSuffix "_TRAIN_NEG" -testPosSuffix "_TEST_POS" -testNegSuffix "_TEST_NEG" > "${OUTPUT_FOLDER}/out.txt"
