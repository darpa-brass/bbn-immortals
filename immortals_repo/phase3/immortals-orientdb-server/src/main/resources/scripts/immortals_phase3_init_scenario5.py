import argparse
import os
import sys
parser = argparse.ArgumentParser('IMMoRTALS Phase 3 Scenario 6 OrientDB Deployment Tool', add_help=True)
parser.add_argument('--host', type=str, required=True, help='The OrientDB host')
parser.add_argument('--port', type=int, required=True, help='The OrientDB port')
parser.add_argument('--username', type=str, required=True, help='The OrientDB user name')
parser.add_argument('--password', type=str, required=True, help='The OrientDB user password')
parser.add_argument('--root-password', type=str, required=True, help='The OrientDB root password')
parser.add_argument('--dau-inventory-xml-file', type=str, required=True, help='The DAU Inventory file')
parser.add_argument('--input-configuration-xml-file', type=str, required=True,
                    help='The input MDLRoot Configuration file')
parser.add_argument('--no-set-ready', action='store_true', help='Do not set the state to "ReadyForAdaptation".')
parser.add_argument('--database-name', type=str, help='The name to use for the database. By default it is "Scenario5"')
args = parser.parse_args()
ENV_VAR_IACS = 'IMMORTALS_ADAPTIVE_CONSTRAINT_SATISFACTION_ROOT'
CONFIG_FILE = 'swri-config.json'
MERGED_INPUT_XML_FILE = 'mdlroot-dauinventory-merged.xml'
BRASS_API_ROOT = (None if os.path.exists('brass_api/translator') else
                  (os.path.join(os.environ[ENV_VAR_IACS], 'src/brass_api_src/')) if ENV_VAR_IACS in os.environ else
                  None)
host = args.host
port = args.port
username = args.username
password = args.password
root_password = args.root_password
dau_inventory_xml_file = args.dau_inventory_xml_file
input_configuration_xml_file = args.input_configuration_xml_file
no_set_ready = args.no_set_ready
database_name = 'Scenario5' if args.database_name is None else args.database_name
if not os.path.exists('brass_api/translator'):
    if ENV_VAR_IACS not in os.environ:
        raise Exception('Cannot find brass_api/translator directory and the environment variable "' + ENV_VAR_IACS +
                        '" is unset!')
    api_path = os.environ[ENV_VAR_IACS]
    if not os.path.isdir(api_path):
        raise Exception('The ' + ENV_VAR_IACS + ' value "' + api_path + '" does not exist!')
    api_path = os.path.join(api_path, 'src/brass_api_src/')
    if not os.path.isdir(api_path):
        raise Exception('The ' + ENV_VAR_IACS + ' child path "' + api_path + '" does not exist!')
    sys.path.append(api_path)
import pyorient
import json
import subprocess
from brass_api.translator.orientdb_importer import OrientDBXMLImporter
# Create and write the configuration file to disk
config = {
    "server": {
        "username": 'root',
        "password": root_password,
        "address": host,
        "port": port
    },
    "database": {
        "username": username,
        "password": password
    }
}
json.dump(config, open(CONFIG_FILE, 'w'), indent=4)
# Merge the DAUInventory into the MDLRoot file to force the SwRI script to load it
print("Merging the DAUInventory into the MDLRoot")
process_output = subprocess.run(
    ["sed",
     "/<\/NameValues>/{\n r " + dau_inventory_xml_file + "\n:a\nn\nba\n}",
     input_configuration_xml_file],
    stdout=subprocess.PIPE)
full_xml_document = process_output.stdout.decode()
out_file = open(MERGED_INPUT_XML_FILE, 'w')
out_file.write(full_xml_document)
out_file.flush()
out_file.close()
## Import the created file containing the inventory and the mdlroot
importer = OrientDBXMLImporter(
    databaseName=database_name,
    configFile=CONFIG_FILE,
    mdlFile=MERGED_INPUT_XML_FILE
)
print("Importing the XML Data. Please wait...")
importer.import_xml()
importer.orientDB_helper.close_database()
# Create the evaluation data structure
client = pyorient.OrientDB(host, port)
client.connect('root', root_password)
# client.db_create('Scenario5', pyorient.DB_TYPE_GRAPH, pyorient.STORAGE_TYPE_MEMORY)
client.db_open(database_name, 'root', root_password)
client.command('CREATE CLASS BBNEvaluationData EXTENDS V')
client.command('CREATE PROPERTY BBNEvaluationData.inputJsonData STRING')
client.command('CREATE PROPERTY BBNEvaluationData.outputJsonData STRING')
client.command('CREATE PROPERTY BBNEvaluationData.currentState STRING')
client.command('CREATE PROPERTY BBNEvaluationData.currentStateInfo STRING')
# Move some things around to restructure where the DAU Inventory is.
client.command('DELETE EDGE FROM (SELECT FROM DAUInventory)')
input_record = client.record_create(-1, {'@BBNEvaluationData': {}})
mdl_record = client.query('SELECT FROM MDLRoot')[0]
inventory_record = client.query('SELECT FROM DAUInventory')[0]
client.command('CREATE EDGE Containment FROM ' + input_record._rid + ' TO ' + mdl_record._rid)
client.command('CREATE EDGE Containment FROM ' + input_record._rid + ' TO ' + inventory_record._rid)
print("XML Data Imported")
if not no_set_ready:
    print("Setting state to 'ReadyForAdaptation'")
    client.command("UPDATE BBNEvaluationData Set currentState = 'ReadyForAdaptation'")
client.close()
