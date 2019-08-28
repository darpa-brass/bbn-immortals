#!/usr/bin/env python3
import argparse

parser = argparse.ArgumentParser('IMMoRTALS Phase 3 Scenario 6 OrientDB Deployment Tool', add_help=True)
parser.add_argument('--host', type=str, required=True, help='The OrientDB host')
parser.add_argument('--port', type=int, required=True, help='The OrientDB port')
parser.add_argument('--username', type=str, required=True, help='The OrientDB user name')
parser.add_argument('--password', type=str, required=True, help='The OrientDB user password')
parser.add_argument('--root-password', type=str, required=True, help='The OrientDB root password')
parser.add_argument('--dau-inventory-xml-file', type=str, required=True, help='The DAU Inventory file')
parser.add_argument('--input-configuration-xml-file', type=str, required=True,
                    help='The input MDLRoot Configuration file')

args = parser.parse_args()

CONFIG_FILE = 'swri-config.json'
MERGED_INPUT_XML_FILE = 'mdlroot-dauinventory-merged.xml'
DATABASE_NAME = 'Scenario5'

host = args.host
port = args.port
username = args.username
password = args.password
root_password = args.root_password
dau_inventory_xml_file = args.dau_inventory_xml_file
input_configuration_xml_file = args.input_configuration_xml_file

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
process_output = subprocess.run(
    ['sed',
     '/<\/NameValues>/{\n r ' + dau_inventory_xml_file + '\n:a\nn\nba\n}',
     input_configuration_xml_file],
    stdout=subprocess.PIPE)
full_xml_document = process_output.stdout.decode()
out_file = open(MERGED_INPUT_XML_FILE, 'w')
out_file.write(full_xml_document)
out_file.flush()
out_file.close()

## Import the created file containing the inventory and the mdlroot
importer = OrientDBXMLImporter(
    databaseName=DATABASE_NAME,
    configFile=CONFIG_FILE,
    mdlFile=MERGED_INPUT_XML_FILE
)
importer.import_xml()
importer.orientDB_helper.close_database()

# Create the evaluation data structure
client = pyorient.OrientDB(host, port)
client.connect('root', root_password)
#client.db_create('Scenario5', pyorient.DB_TYPE_GRAPH, pyorient.STORAGE_TYPE_MEMORY)
client.db_open('Scenario5', 'root', root_password)
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
client.command("UPDATE BBNEvaluationData Set currentState = 'ReadyForAdaptation'")
client.close()
