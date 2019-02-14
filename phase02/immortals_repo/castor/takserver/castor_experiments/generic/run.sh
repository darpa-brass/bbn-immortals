OUTPUT_FOLDER="out"
TARGET="query1_all"
POS_EXAMPLES="examples/query1_all_pos.csv"
NEG_EXAMPLES="examples/query1_all_neg.csv"

mkdir $OUTPUT_FOLDER

# Generate dataModel file
java -cp ../../../Castor/Castor.jar castor.clients.PreprocessClient -metadata ../../metadata.json -target ${TARGET} -examplesFile ${POS_EXAMPLES} -ddl ../../ddl-schema-baseline-strings.sql -outputModes castor-input/dataModel.json

# Run Castor
echo "Running Castor..."
java -jar ../../../Castor/Castor.jar -parameters castor-input/parameters.json -ddl ../../ddl-schema-baseline-strings.sql -dataModel castor-input/datamodel.json -posTrainExamplesFile ${POS_EXAMPLES} -negTrainExamplesFile ${NEG_EXAMPLES} -outputDefinitionFile "${OUTPUT_FOLDER}/definition.txt" -outputSQL > "${OUTPUT_FOLDER}/out.txt"
