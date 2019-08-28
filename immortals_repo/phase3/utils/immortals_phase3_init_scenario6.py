#!/usr/bin/env python3
import argparse

parser = argparse.ArgumentParser('IMMoRTALS Phase 3 Scenario 6 OrientDB Deployment Tool', add_help=True)
parser.add_argument('--host', type=str, required=True, help='The OrientDB host')
parser.add_argument('--port', type=int, required=True, help='The OrientDB port')
parser.add_argument('--username', type=str, required=True, help='The OrientDB user name')
parser.add_argument('--password', type=str, required=True, help='The OrientDB user password')
parser.add_argument('--root-password', type=str, required=True, help='The OrientDB root password')
parser.add_argument('--input-json-file', type=str, required=True, help='The input JSON configuration file')

args = parser.parse_args()
host = args.host
port = args.port
username = args.username
password = args.password
root_password = args.root_password
input_json_file = args.input_json_file

import pyorient

client = pyorient.OrientDB(host, port)
client.connect('root', root_password)
client.db_create('Scenario6', pyorient.DB_TYPE_GRAPH, pyorient.STORAGE_TYPE_MEMORY)
client.db_open('Scenario6', 'root', root_password)
client.db_open('Scenario6', username, password)
client.command('CREATE CLASS BBNEvaluationData EXTENDS V')
client.command('CREATE PROPERTY BBNEvaluationData.inputJsonData STRING')
client.command('CREATE PROPERTY BBNEvaluationData.outputJsonData STRING')
client.command('CREATE PROPERTY BBNEvaluationData.currentState STRING')
client.command('CREATE PROPERTY BBNEvaluationData.currentStateInfo STRING')

json_data = open(input_json_file).read()
client.record_create(
    -1,
    {
        '@BBNEvaluationData': {
            'inputJsonData': json_data
        }
    }
)
client.command("UPDATE BBNEvaluationData Set currentState = 'ReadyForAdaptation'")
client.close()
