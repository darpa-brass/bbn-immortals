#!/usr/bin/env python3

import os
import re
import subprocess
import time
from typing import List, Optional

import pyorient as pyorient
from pyorient import OrientDB

from odbhelper.brass_api_helper import BrassApiHelper
from odbhelper.datatypes import STARTUP_TIMEOUT_S, SHUTDOWN_TIMEOUT_S, STDOUT_LOG_BASE_NAME, \
    STDERR_LOG_BASE_NAME, Scenario, ScenarioType

SCRIPT_DIRECTORY = os.path.dirname(os.path.realpath(__file__))

ODB_STARTED_REGEX = r'[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3} INFO  OrientDB Server is active.*'

BBN_PERSISTENT_DB_NAME = "BBNPersistent"

XML_COMBINED_FILE = 'mdl_combined.xml'


def load_xml_into_db(host: str, port: int, root_password: str, xml_filepath: str, db_identifier: str):
    bah = BrassApiHelper("root", root_password, host, port)

    bah.load_xml_into_db(xml_filepath, db_identifier)

    print('File    "' + xml_filepath + '" loaded into database "' + db_identifier + '".')


def init_db_data(host: str, port: int, root_password: str, db_name: str,
                 xml_files: Optional[List[str]] = None, client: OrientDB = None) -> pyorient.OrientDB:
    if xml_files is not None:
        for filepath in xml_files:
            load_xml_into_db(host, port, root_password, filepath, db_name)

    if client is None:
        client = pyorient.OrientDB(host, port)
        client.connect('root', root_password)

    if client.db_exists(db_name):
        client.db_open(db_name, 'admin', 'admin')

    else:
        client.db_create(db_name, pyorient.DB_TYPE_GRAPH, pyorient.STORAGE_TYPE_MEMORY)

    client.db_open(db_name, 'admin', 'admin')
    client.command('CREATE CLASS BBNEvaluationInput EXTENDS V')
    client.command('CREATE PROPERTY BBNEvaluationInput.jsonData STRING')

    client.command('CREATE CLASS BBNEvaluationOutput EXTENDS V')
    client.command('CREATE PROPERTY BBNEvaluationOutput.jsonData STRING')
    client.command('CREATE PROPERTY BBNEvaluationOutput.finalState STRING')
    client.command('CREATE PROPERTY BBNEvaluationOutput.finalStateInfo STRING')

    # client.coommand('CREATE CLASS DAUInventory EXTENDS V')
    return client


def init_bbn_persistent(host: str, port: int, root_password: str):
    client = init_db_data(host, port, root_password, BBN_PERSISTENT_DB_NAME)
    client.command('CREATE PROPERTY BBNEvaluationInput.evaluationInstanceIdentifier STRING')
    client.command('CREATE PROPERTY BBNEvaluationOutput.evaluationInstanceIdentifier STRING')
    client.close()


def init_scenario5(host: str, port: int, root_password: str, scenario: Scenario):
    output_file = 'ORIENTDB_INPUT_DATA.xml'

    out = subprocess.run(
        ['sed', '/<\/NameValues>/{\n r ' + SCRIPT_DIRECTORY + "/" + scenario.xmlInventoryPath + '\n:a\nn\nba\n}',
         SCRIPT_DIRECTORY + "/" + scenario.xmlMdlrootInputPath],
        stdout=subprocess.PIPE)

    out_file = open(output_file, 'w')
    out_file.write(out.stdout.decode())
    out_file.flush()
    out_file.close()

    client = init_db_data(
        host, port, root_password, scenario.dbName,
        [
            output_file
        ]
    )  # type: OrientDB

    # TODO: This is hacky. I shouldn't have to import it has the child of a GenericParameter and then disconnect it!
    client.command('DELETE EDGE FROM (SELECT FROM DAUInventory)')

    input_record = client.record_create(-1, {'@BBNEvaluationInput': {}})
    mdl_record = client.query('SELECT FROM MDLRoot')[0]
    client.command('CREATE EDGE Containment FROM ' + input_record._rid + ' TO ' + mdl_record._rid)
    client.record_create(-1, {'@BBNEvaluationOutput': {}})

    client.close()


def init_scenario6(host: str, port: int, root_password: str, scenario: Scenario):
    client = init_db_data(host, port, root_password, scenario.dbName)

    json_data = open(SCRIPT_DIRECTORY + "/" + scenario.jsonInputPath, 'r').read()

    client.record_create(
        -1,
        {
            '@BBNEvaluationInput': {
                'jsonData': json_data
            }
        }
    )
    client.record_create(-1, {'@BBNEvaluationOutput': {}})

    client.close()


class OdbStarter:
    def __init__(self, host: str, port: int, orientdb_home: str, orientdb_root_password: str):
        self.host = host
        self.port = port
        self.orientdb_home = orientdb_home
        self.orientdb_root_password = orientdb_root_password
        self.start_timestamp = time.strftime('%Y%m%d_%H%M%S')
        self._odb_process = None
        self._odb_stdout = None
        self._odb_stderr = None

    def start_db(self, unique_log: bool):
        stdout_file = STDOUT_LOG_BASE_NAME % (('-' + self.start_timestamp) if unique_log else '')
        stderr_file = STDERR_LOG_BASE_NAME % (('-' + self.start_timestamp) if unique_log else '')

        self._odb_stdout = open(stdout_file, 'w', 1)
        self._odb_stderr = open(stderr_file, 'w', 1)

        print('Starting OrientDB. See "' + stdout_file + "' and '"
              + stderr_file + "' for logs.")

        self._odb_process = subprocess.Popen(
            ['bash', os.path.join(self.orientdb_home, 'bin', 'server.sh')],
            env={
                'ORIENTDB_ROOT_PASSWORD': self.orientdb_root_password
            },
            stdout=self._odb_stdout,
            stderr=self._odb_stderr
        )

        read_file = open(stderr_file, 'r')

        wait_time_seconds = STARTUP_TIMEOUT_S
        while wait_time_seconds > 0:
            for line in read_file.readlines():

                if re.match(ODB_STARTED_REGEX, line) is not None:
                    print("OrientDB has been started successfully.")
                    return

            time.sleep(0.2)
            wait_time_seconds = wait_time_seconds - 0.2

        print("FAILED TO START ORIENTDB within the expected time!")
        read_file.close()
        self._odb_process.kill()

    def init_bbn_persistent(self, recreate: bool = False):
        client = pyorient.OrientDB(self.host, self.port)
        client.connect("root", self.orientdb_root_password)
        db_exists = client.db_exists(BBN_PERSISTENT_DB_NAME)

        if recreate and db_exists:
            print('Removing ' + BBN_PERSISTENT_DB_NAME + ' database.')
            client.db_drop(BBN_PERSISTENT_DB_NAME)
            db_exists = False

        if not db_exists:
            print('Initializing ' + BBN_PERSISTENT_DB_NAME + ' database.')
            client = init_db_data(self.host, self.port, self.orientdb_root_password, BBN_PERSISTENT_DB_NAME)
            client.command('CREATE PROPERTY BBNEvaluationInput.evaluationInstanceIdentifier STRING')
            client.command('CREATE PROPERTY BBNEvaluationOutput.evaluationInstanceIdentifier STRING')
            client.close()

    def init_test_scenarios(self, reset_scenarios: Optional[List[Scenario]] = None):
        print('Initializing OrientDB Databases...')

        client = pyorient.OrientDB(self.host, self.port)
        client.connect("root", self.orientdb_root_password)

        if reset_scenarios is not None:
            for scenario in reset_scenarios:
                if client.db_exists(scenario.dbName):
                    print('Removing database for "' + scenario.dbName + '".')
                    client.db_drop(scenario.dbName)

        client.close()

        for scenario in reset_scenarios:
            if scenario.scenarioType == ScenarioType.Scenario5:
                init_scenario5(self.host, self.port, self.orientdb_root_password, scenario)
                pass
            elif scenario.scenarioType == ScenarioType.Scenario6:
                init_scenario6(self.host, self.port, self.orientdb_root_password, scenario)

        print('Finished initializing OrientDB')

    def wait(self):
        if self._odb_process is not None:
            self._odb_process.wait()

    def stop(self):
        if self._odb_process is not None:
            print('Shutting down OrientDB...')

            expiration = SHUTDOWN_TIMEOUT_S

            self._odb_process.terminate()

            while expiration > 0:
                status = self._odb_process.poll()

                if status is None:
                    time.sleep(0.2)
                    expiration = expiration - 0.2

                else:
                    print('OrientDB has been successfully shut down.')
                    return

            print("OrientDB not shutting down gracefully. Killing it...")
            self._odb_process.kill()
