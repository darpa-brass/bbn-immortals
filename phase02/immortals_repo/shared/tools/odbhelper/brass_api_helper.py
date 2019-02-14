#!/usr/bin/env python3
import json

from brass_api.translator.orientdb_importer import OrientDBXMLImporter

SWRI_CONFIG_FILE = 'swri_api_helper_config.json'


class BrassApiHelper:
    def __init__(self, server_username: str, server_password: str, server_address: str, server_port: int):
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

    def load_xml_into_db(self, xml_filepath: str, db_identifier: str):
        json.dump(self.config, open(SWRI_CONFIG_FILE, 'w'), indent=4)

        importer = OrientDBXMLImporter(
            databaseName=db_identifier,
            configFile=SWRI_CONFIG_FILE,
            mdlFile=xml_filepath
        )

        importer.import_xml()
        importer.orientDB_helper.close_database()

        print('File    "' + xml_filepath + '" loaded into database "' + db_identifier + '".')
