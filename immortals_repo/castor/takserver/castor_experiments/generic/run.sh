OUTPUT_FOLDER="out"

mkdir $OUTPUT_FOLDER

# Without -globalDefinition option
java -jar ../../../Castor/Castor.jar -parameters castor-input/parameters.json -inds castor-input/inds.json -dataModel castor-input/datamodel.json -posTrainExamplesFile examples/query1_all_pos.csv -negTrainExamplesFile examples/query1_all_neg.csv -outputDefinitionFile "${OUTPUT_FOLDER}/definition.txt" -outputSQL > "${OUTPUT_FOLDER}/out.txt"

# With -globalDefinition option
#java -jar ../../../Castor/Castor.jar -parameters castor-input/parameters.json -inds castor-input/inds.json -dataModel castor-input/datamodel.json -posTrainExamplesFile examples/query1_all_pos.csv -negTrainExamplesFile examples/query1_all_neg.csv -outputDefinitionFile "${OUTPUT_FOLDER}/definition.txt" -outputSQL -globalDefinition > "${OUTPUT_FOLDER}/out.txt"