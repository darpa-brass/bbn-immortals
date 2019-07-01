#!/usr/bin/env python3
import json
import os
import subprocess
from typing import Optional, List

import pyorient
from brass_api.translator.orientdb_importer import OrientDBXMLImporter
from odb.datatypes import Scenario, ScenarioType

SWRI_CONFIG_FILE = 'swri_api_helper_config.json'

IMMORTALS_ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.realpath(__file__)), '../../../'))


class BrassApiHelper:
    def __init__(self, server_username: str, server_password: str, server_address: str, server_port: int):
        self.host = server_address
        self.port = server_port
        self.root_password = server_password

        self.config = {
            "server": {
                "username": server_username,
                "password": server_password,
                "address": server_address,
                "port": server_port
            },
            "database": {
                "username": "admin",
                "password": "admin"
            }
        }

    def _init_db_data(self, db_name: str, xml_files: Optional[List[str]] = None,
                      client: pyorient.OrientDB = None) -> pyorient.OrientDB:
        if xml_files is not None:
            for filepath in xml_files:
                json.dump(self.config, open(SWRI_CONFIG_FILE, 'w'), indent=4)

                importer = OrientDBXMLImporter(
                    databaseName=db_name,
                    configFile=SWRI_CONFIG_FILE,
                    mdlFile=filepath
                )

                importer.import_xml()
                importer.orientDB_helper.close_database()

        if client is None:
            client = pyorient.OrientDB(self.host, self.port)
            client.connect('root', self.root_password)

        if client.db_exists(db_name):
            client.db_open(db_name, 'admin', 'admin')
        else:
            client.db_create(db_name, pyorient.DB_TYPE_GRAPH, pyorient.STORAGE_TYPE_MEMORY)

        client.db_open(db_name, 'admin', 'admin')
        client.command('CREATE CLASS BBNEvaluationData EXTENDS V')
        client.command('CREATE PROPERTY BBNEvaluationData.inputJsonData STRING')
        client.command('CREATE PROPERTY BBNEvaluationData.outputJsonData STRING')
        client.command('CREATE PROPERTY BBNEvaluationData.currentState STRING')
        client.command('CREATE PROPERTY BBNEvaluationData.currentStateInfo STRING')
        return client

    def init_test_scenarios(self, scenarios: Optional[List[Scenario]]):
        print('Initializing OrientDB Databases on ' + self.host + ':' + str(self.port) + '...')

        client = pyorient.OrientDB(self.host, self.port)
        client.connect("root", self.root_password)

        for scenario in scenarios:
            if client.db_exists(scenario.dbName):
                print('Removing database for "' + scenario.dbName + '".')
                client.db_drop(scenario.dbName)
                print('Database removed.')

        client.close()

        for scenario in scenarios:
            if scenario.scenarioType == ScenarioType.Scenario5:
                print('Initializing database "' + scenario.dbName + '"...')
                output_file = 'ORIENTDB_INPUT_DATA.xml'

                out = subprocess.run(
                    ['sed',
                     '/<\/NameValues>/{\n r ' + os.path.join(IMMORTALS_ROOT,
                                                             scenario.xmlInventoryPath) + '\n:a\nn\nba\n}',
                     os.path.join(IMMORTALS_ROOT, scenario.xmlMdlrootInputPath)],
                    stdout=subprocess.PIPE)

                out_file = open(output_file, 'w')
                out_file.write(out.stdout.decode())
                out_file.flush()
                out_file.close()

                client = self._init_db_data(scenario.dbName, [output_file])  # type: pyorient.OrientDB

                # TODO: This is hacky. I shouldn't have to import it has the child of a GenericParameter and then disconnect it!
                client.command('DELETE EDGE FROM (SELECT FROM DAUInventory)')

                input_record = client.record_create(-1, {'@BBNEvaluationData': {}})
                mdl_record = client.query('SELECT FROM MDLRoot')[0]
                client.command('CREATE EDGE Containment FROM ' + input_record._rid + ' TO ' + mdl_record._rid)

                client.command("UPDATE BBNEvaluationData Set currentState = 'ReadyForAdaptation'")
                client.close()

                print('Database initialization finished.')

            elif scenario.scenarioType == ScenarioType.Scenario6:
                print('Initializing database "' + scenario.dbName + '"...')
                client = self._init_db_data(scenario.dbName)

                json_data = open(os.path.join(IMMORTALS_ROOT, scenario.jsonInputPath)).read()

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

                print('Database initialization finished.')
