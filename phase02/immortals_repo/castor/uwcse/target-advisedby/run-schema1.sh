FOLDS=5
OUTPUT_FOLDER="out"

mkdir $OUTPUT_FOLDER

for i in `seq 1 $FOLDS`;
do
	echo "Fold $i"
	java -jar ../../Castor/Castor.jar -parameters castor-input/parameters.json -inds castor-input/inds-schema1.json -dataModel castor-input/datamodel-schema1.json -trainPosSuffix "_FOLD${i}_TRAIN_POS" -trainNegSuffix "_FOLD${i}_TRAIN_NEG" -testPosSuffix "_FOLD${i}_TEST_POS" -testNegSuffix "_FOLD${i}_TEST_NEG" > "${OUTPUT_FOLDER}/fold${i}.txt"
done
