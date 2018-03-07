#! /bin/sh

CASTOR_FOLDER=${1}
TARGET="${2}"
POS_EXAMPLES="${CASTOR_FOLDER}/positive_data.csv"
NEG_EXAMPLES="${CASTOR_FOLDER}/negative_data.csv"
DDL="${CASTOR_FOLDER}/ddl.sql"
PARAMETERS="${CASTOR_FOLDER}/parameters.json"

#rm "${CASTOR_FOLDER}/definition.txt"
#rm "${CASTOR_FOLDER}/out.txt"

echo "Castor submission folder: ${CASTOR_FOLDER}"
echo "VoltDB Home: ${VOLTDB_HOME}"
echo "Positive Training Records: ${POS_EXAMPLES}"
echo "Negative Training Records: ${NEG_EXAMPLES}"
echo "Parameters: ${PARAMETERS}"

# Generate dataModel file
echo "Generating data model..."
java -cp ../../../Castor/Castor.jar castor.clients.PreprocessClient -metadata ../../metadata.json -target ${TARGET} -examplesFile ${POS_EXAMPLES} -ddl "${DDL}" -outputModes ${CASTOR_FOLDER}/dataModel.json

# Run Castor
echo "Running Castor..."
java -Xmx4g -jar ../../../Castor/Castor.jar -parameters "${PARAMETERS}" -ddl "${DDL}" -dataModel "${CASTOR_FOLDER}/dataModel.json" -posTrainExamplesFile "${POS_EXAMPLES}" -negTrainExamplesFile "${NEG_EXAMPLES}" -outputDefinitionFile "${CASTOR_FOLDER}/definition.txt" -outputSQL > "${CASTOR_FOLDER}/out.txt"