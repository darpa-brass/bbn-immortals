#!/usr/bin/env python3

import argparse
import asyncio
import json
import logging
import os
import re
import sys
import uuid
from typing import Dict
from urllib import parse

if os.path.abspath(os.path.join(os.path.dirname(os.path.realpath(__file__)), os.pardir)) not in sys.path:
    sys.path.append(os.path.abspath(os.path.join(os.path.dirname(os.path.realpath(__file__)), os.pardir)))

SERVER_HOST = '128.89.138.84'
# SERVER_HOST = '0.0.0.0'
WEBSOCKET_PORT = 8765
REST_PORT = 8867

SERVER_URL = 'ws://' + SERVER_HOST + ':' + str(WEBSOCKET_PORT)

SSH_PATH = os.path.join(os.environ['HOME'], '.ssh')
SSH_KEY_PATH = os.path.join(SSH_PATH, 'immortals_vine_id_rsa')

_parser = None

_stdout_websocket_identifier = None
_stdout_websocket = None

_last_msg = None

_create_testbed_plain_autoconf_msg = """
Please append `Include immortals_vine_%s_config` to ~/.ssh/config to access
the following machines on the testbed with the corresponding commands:

    DAS: `ssh %sdas`

The vine dashboard for the testbed can be found at the following URL:
    %s/#/testbeds/%s/machines

Please don't forget to delete your testbed using this tool when you are finished.
"""

_create_testbed_android_autoconf_msg = """
Please append `Include immortals_vine_%s_config` to ~/.ssh/config to access
the following machines on the testbed with the corresponding commands:

    DAS: `ssh %sdas`
    Android Emulator 00 Host: `ssh %sandroid0`
    Android Emulator 01 Host: `ssh %sandroid1`

To connect to the android emulators, connect to them using adb as follows:
    Android Emulator 00: `adb connect %s:5432`
    Android Emulator 01: `adb connect %s:5432`

The vine dashboard for the testbed can be found at the following URL:
    %s/#/testbeds/%s/machines

Please don't forget to delete your testbed using this tool when you are finished.
"""


def init_parser(parent_parser=None):
    global _parser

    if parent_parser is None:
        _parser = argparse.ArgumentParser(description='IMMoRTALS BBN Vine Utility')
    else:
        _parser = parent_parser.add_parser('buildsystem', help='IMMoRTALS BBN Vine Utility')
    _parser.add_argument('testbed_name', metavar='TESTBED_NAME', type=str,
                         help='The testbed to work with. the value ' +
                              'of the "USER" environment variable will automatically be prepended.')

    _group = _parser.add_mutually_exclusive_group()
    _group.add_argument('--create-testbed', '-c', action='store_true',
                        help='Creates a testbed on the vine cluster containing A DAS and no Android emulators.')

    _group.add_argument('--create-android-testbed', action='store_true',
                        help='Creates a testbed on the vine cluster containing A DAS and two Android emulators.')

    _group.add_argument('--delete-testbed', '-D', action='store_true',
                        help='Deletes an immortals testbed on the vine cluster')
    _group.add_argument(
        '--create-testbed-autoconf', action='store_true',
        help='Creates a testbed on the vine cluster containing A DAS and no Android emulators. ' +
             'Additionally creates a "~/.ssh/immortals_vine_<testbed lowercase name>_config" file that can be added ' +
             'to your ~/.ssh/config to connect to the testbed with the following line:'
             'Include <filepath>')

    _group.add_argument(
        '--create-android-testbed-autoconf', action='store_true',
        help='Creates a testbed on the vine cluster containing A DAS and two Android emulators. ' +
             'Additionally creates a "~/.ssh/immortals_vine_<testbed lowercase name>_config" file that can be added ' +
             'to your ~/.ssh/config to connect to the testbed with the following line:'
             'Include <filepath>')

    _group.add_argument('--das-repo-update', action='store_true',
                        help='Updates the repository on the testbed DAS.')

    _group.add_argument('--das-deploy', action='store_true',
                        help='Executes the deploy task on the DAS, performing all build and analysis tasks.')
    _group.add_argument('--das-execute-test', type=str, metavar="TEST_IDENTIFIER",
                        choices=[
                            'p2cp1',
                            'p2cp1BaselineA',
                            'p2cp1BaselineB',
                            'p2cp1Challenge',
                            'p2cp2',
                            'p2cp2BaselineA',
                            'p2cp2BaselineB',
                            'p2cp2Challenge',
                            'p2cp3plu',
                            'p2cp3pluBaselineA',
                            'p2cp3pluBaselineB',
                            'p2cp3pluChallenge',
                            'p2cp3hddrass',
                            'p2cp3hddrassBaselineA',
                            'p2cp3hddrassBaselineB',
                            'p2cp3hddrassChallenge',
                            'p2cp3pql',
                            'p2cp3pqlBaselineA',
                            'p2cp3pqlBaselineB',
                            'p2cp3pqlChallenge'
                        ])

    _group.add_argument('--fetch-test-results', type=str, help='TBD')

    _parser.add_argument('--machine-mode', '-m', action='store_true',
                         help="Returns data as formatted JSON with select commands.")
    _parser.add_argument('--specify-cp-profile', type=str,
                         choices=['all', 'p2', 'p2cp1', 'p2cp2', 'p2cp3hddrass', 'p2cp3plu', 'p2cp3pql', 'p3', 'p3cp1',
                                  'p3cp2', 'p3cp3'],
                         help='Applies a subset of applications and analysis to operations when applicable to ' +
                              'significantly shorten duration of operations. By deafault everything is enabled. ' +
                              'Only applicable to the --das-deploy and --das-execute-test commands.')
    _parser.add_argument('--branch', type=str,
                         help='Specifies the branch to use. Only applicable to the --das-update, --das-deploy, and ' +
                              '--das-execute-test commands.')

    _parser.add_argument('--verbose', '-v', action='store_true', help='Increases logging verbosity.')

    _group.add_argument('--jenkins-add-plain-testbed', action='store_true', help=argparse.SUPPRESS)
    _group.add_argument('--jenkins-add-android-testbed', action='store_true', help=argparse.SUPPRESS)
    _group.add_argument('--jenkins-claim-plain-testbed', action='store_true', help=argparse.SUPPRESS)
    _group.add_argument('--jenkins-claim-android-testbed', action='store_true', help=argparse.SUPPRESS)
    _group.add_argument('--jenkins-replace-testbed', action='store_true', help=argparse.SUPPRESS)
    _group.add_argument('--jenkins-replace-testbed-nowait', action='store_true', help=argparse.SUPPRESS)
    _group.add_argument('--jenkins-autoconf', action='store_true', help=argparse.SUPPRESS)

    _group.add_argument('--jenkins-start-testbed-coordinator', action='store_true', help=argparse.SUPPRESS)
    _group.add_argument('--jenkins-perform-predeploy-rebuild', action='store_true', help=argparse.SUPPRESS)
    return _parser


def main(parser_args):
    import importlib.util
    missing_libs = list()
    if importlib.util.find_spec('requests') is None:
        missing_libs.append('requests==2.18.4')
    if importlib.util.find_spec('sanic') is None:
        missing_libs.append('sanic==0.7.0')
    if importlib.util.find_spec('websockets') is None:
        missing_libs.append('websockets==6.0')

    if len(missing_libs) > 0:
        print("ERROR: Missing required libraries! Please install using the following command::")
        print('    ' + sys.executable + ' -m pip install ' + ' '.join(missing_libs))
        exit(1)

    import websockets
    from buildsystem.datatypes import objectify, StatusNotification
    from buildsystem.testbedmanager import DasTestbed
    from buildsystem.vine import VINE_ROOT_URL

    if parser_args.verbose:
        logging.basicConfig(level=logging.DEBUG)
    else:
        logging.basicConfig(level=logging.INFO)

    async def _connect_stdout():
        global _stdout_websocket, _stdout_websocket_identifier

        async def print_from_websocket(ws):

            try:
                while True:
                    msg = await ws.recv()
                    print(msg)

            except websockets.ConnectionClosed:
                pass

        websocket = await websockets.connect(SERVER_URL + '/stdoutListener', timeout=86400)
        _stdout_websocket = websocket
        _stdout_websocket_identifier = await websocket.recv()
        asyncio.ensure_future(print_from_websocket(websocket))

    async def _make_request(command: str, machine_readable: bool, request_params: Dict):
        global _last_msg, _stdout_websocket_identifier

        await _connect_stdout()

        param_tuples = ()
        for key in request_params:
            param_tuples = param_tuples + ((key, request_params[key]),)

        if _stdout_websocket_identifier is not None:
            param_tuples = param_tuples + (('stdout_identifier', _stdout_websocket_identifier),)

        msg = None

        async with websockets.connect(
                SERVER_URL + '/' + command + '?' + parse.urlencode(param_tuples), timeout=86400) as websocket:
            try:
                while True:
                    msg = await websocket.recv()
                    if machine_readable:
                        print(msg)
                    else:
                        rcv_data = objectify(msg, StatusNotification)  # type: StatusNotification
                        print(rcv_data.message)
            except websockets.ConnectionClosed:
                if _stdout_websocket is not None:
                    await _stdout_websocket.close()

        _last_msg = msg

    def create_unmanaged_testbed(include_android: bool, autoconf: bool):
        if autoconf and not os.path.exists(SSH_KEY_PATH):
            print('ERROR: Please add an authorized key to testbed images and ensure the private key exists at "' +
                  SSH_KEY_PATH + '"!')
            exit(1)

        asyncio.get_event_loop().run_until_complete(_make_request(
            'createTestbed', mm,
            {'testbed_name': user + '_' + parser_args.testbed_name,
             'include_android': include_android}
        ))

        if autoconf:
            rx_msg_data = json.loads(_last_msg)

            if 'status' not in rx_msg_data or rx_msg_data['status'] != 'TESTBED_READY' or rx_msg_data['data'] is None:
                print('Cannot create autoconf file since the server did not return all required information!')

            rx_data = rx_msg_data['data']
            rx_tb = objectify(rx_data, DasTestbed)
            save_ssh_config_and_display_usage(rx_tb, parser_args.testbed_name.lower())

    def save_ssh_config_and_display_usage(target_testbed: DasTestbed, config_identifier: str = None):
        if not os.path.exists(SSH_PATH):
            print('Cannot create autoconf file since the directory "' + SSH_PATH + '" does not exist!')
            return

        das_ip = target_testbed.get_das_vm().vm_details.public_ip
        has_android = target_testbed.has_android_vms()
        testbed_id = target_testbed.testbed.testbed_id
        testbed_name = target_testbed.testbed.testbed_details.testbed_name

        if config_identifier is None:
            config_identifier = testbed_name.lower()

        ssh_file_lines = list()

        ssh_file_lines.append('Host ' + config_identifier + 'das\n')
        ssh_file_lines.append('  HostName ' + das_ip + '\n')
        ssh_file_lines.append('  IdentityFile ' + SSH_KEY_PATH + '\n')
        ssh_file_lines.append('  User ubuntu\n')
        ssh_file_lines.append('  StrictHostKeyChecking no\n')

        if has_android:
            android0_ip = target_testbed.get_android0_vm().vm_details.public_ip
            android1_ip = target_testbed.get_android1_vm().vm_details.public_ip

            ssh_file_lines.append('Host ' + config_identifier + 'android0\n')
            ssh_file_lines.append('  HostName ' + android0_ip + '\n')
            ssh_file_lines.append('  IdentityFile ' + SSH_KEY_PATH + '\n')
            ssh_file_lines.append('  User ubuntu\n')
            ssh_file_lines.append('  StrictHostKeyChecking no\n')

            ssh_file_lines.append('Host ' + config_identifier + 'android1\n')
            ssh_file_lines.append('  HostName ' + android1_ip + '\n')
            ssh_file_lines.append('  IdentityFile ' + SSH_KEY_PATH + '\n')
            ssh_file_lines.append('  User ubuntu\n')
            ssh_file_lines.append('  StrictHostKeyChecking no\n')

            display_msg = _create_testbed_android_autoconf_msg % (
                config_identifier, config_identifier, config_identifier, config_identifier, android1_ip, android0_ip,
                VINE_ROOT_URL,
                str(testbed_id))

        else:
            display_msg = _create_testbed_plain_autoconf_msg % (
                config_identifier, config_identifier, VINE_ROOT_URL, str(testbed_id))

        if len(ssh_file_lines) > 0:
            with open(os.path.join(SSH_PATH,
                                   'immortals_vine_' + config_identifier + '_config'),
                      'w') as file:
                file.writelines(ssh_file_lines)

            print(display_msg)

    if re.search('^[a-zA-Z0-9_]*$', parser_args.testbed_name) is None:
        print("ERROR: Testbed Must contain only nunbers, letters, or an underscore!")
        exit(1)

    if parser_args.testbed_name == "null" or parser_args.testbed_name == "None":
        print('ERROR: "null" and "None" cannot be used for testbed names!')
        exit(1)

    mm = parser_args.machine_mode

    user = os.environ['USER']
    if user is None:
        user = 'user' + str(uuid.uuid4())[0:8]

    # Jekins specific commands
    if parser_args.jenkins_add_plain_testbed:
        asyncio.get_event_loop().run_until_complete(_make_request(
            'addPlainTestbedToBuildPool', mm,
            {}
        ))

    elif parser_args.jenkins_add_android_testbed:
        asyncio.get_event_loop().run_until_complete(_make_request(
            'addAndroidTestbedToBuildPool', mm,
            {}
        ))

    elif parser_args.jenkins_replace_testbed:
        asyncio.get_event_loop().run_until_complete(_make_request(
            'replaceTestbedInBuildPool', mm,
            {'testbed_name': 'jenkins_' + parser_args.testbed_name}
        ))

    elif parser_args.jenkins_replace_testbed_nowait:
        asyncio.get_event_loop().run_until_complete(_make_request(
            'replaceTestbedInBuildPoolNoWait', mm,
            {'testbed_name': 'jenkins_' + parser_args.testbed_name}
        ))

    elif parser_args.jenkins_claim_plain_testbed:
        asyncio.get_event_loop().run_until_complete(_make_request(
            'claimBuildpoolPlainTestbed', mm,
            {'testbed_name': 'jenkins_' + parser_args.testbed_name}
        ))

    elif parser_args.jenkins_claim_android_testbed:
        asyncio.get_event_loop().run_until_complete(_make_request(
            'claimBuildpoolAndroidTestbed', mm,
            {'testbed_name': 'jenkins_' + parser_args.testbed_name}
        ))

    elif parser_args.jenkins_start_testbed_coordinator:
        from buildsystem import server
        server.main()

    elif parser_args.jenkins_perform_predeploy_rebuild:
        raise NotImplementedError
        # asyncio.get_event_loop().run_until_complete(_make_request(
        #     'rebuildPredeployImage', mm,
        #     {'testbed_name': user + '_' + parser_args.testbed_name}
        # ))

    elif parser_args.jenkins_autoconf:
        asyncio.get_event_loop().run_until_complete(_make_request(
            'getExistingTestbed', mm,
            {'testbed_name': 'jenkins_' + parser_args.testbed_name}
        ))

        msg_data = json.loads(_last_msg)

        if 'status' not in msg_data or msg_data['status'] != 'TESTBED_READY' or msg_data['data'] is None:
            print('Cannot create autoconf file since the server did not return all required information!')

        data = msg_data['data']
        testbed = objectify(data, DasTestbed)
        save_ssh_config_and_display_usage(testbed, 'jenkinstemp')

    # General commands
    elif parser_args.create_testbed:
        create_unmanaged_testbed(False, False)

    elif parser_args.create_android_testbed:
        create_unmanaged_testbed(True, False)

    elif parser_args.delete_testbed:
        asyncio.get_event_loop().run_until_complete(_make_request(
            'deleteTestbed', mm,
            {'testbed_name': user + '_' + parser_args.testbed_name}
        ))

    elif parser_args.create_testbed_autoconf:
        create_unmanaged_testbed(False, True)

    elif parser_args.create_android_testbed_autoconf:
        create_unmanaged_testbed(True, True)

    elif parser_args.das_repo_update:
        params = {
            'testbed_name': user + '_' + parser_args.testbed_name
        }

        if parser_args.branch is not None:
            params['branch'] = parser_args.branch

        asyncio.get_event_loop().run_until_complete(_make_request('updateRepo', mm, params))

    elif parser_args.das_execute_test is not None:
        params = {
            'testbed_name': user + '_' + parser_args.testbed_name,
            'test_identifier': parser_args.das_execute_test
        }
        if parser_args.specify_cp_profile is not None:
            params['cp_profile'] = parser_args.specify_cp_profile
        if parser_args.branch is not None:
            params['branch'] = parser_args.branch

        asyncio.get_event_loop().run_until_complete(_make_request('dasExecuteTest', mm, params))

    elif parser_args.das_deploy:
        params = {
            'testbed_name': user + '_' + parser_args.testbed_name,
        }
        if parser_args.specify_cp_profile is not None:
            params['cp_profile'] = parser_args.specify_cp_profile
        if parser_args.branch is not None:
            params['branch'] = parser_args.branch
        asyncio.get_event_loop().run_until_complete(_make_request(
            'dasDeploy', mm,
            params
        ))

    else:
        print('Unexpected parameters.')
        exit(1)

    notification = objectify(json.loads(_last_msg), StatusNotification)  # type: StatusNotification
    exit(notification.status.error_code)


if __name__ == '__main__':
    _parser = argparse.ArgumentParser()
    subparser = _parser.add_subparsers(dest='cmd')
    init_parser(subparser)
    args = _parser.parse_args()
    main(args)
