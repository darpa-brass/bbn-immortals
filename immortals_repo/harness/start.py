#!/usr/bin/env python

import argparse
import logging
import os
import subprocess
import sys

from pymmortals import immortalsglobals as ig
from pymmortals import threadprocessrouter as tpr
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.routing import EventType, EventTags
from pymmortals.testharnessconnector import ll_rest_endpoint
from pymmortals.testharnessconnector import th_client

_fuseki_process = None
_repository_service_process = None
_das_process = None
_rest_endpoint_process = None

_das_alt_endpoint_process = None

_rest_server = None

parser = argparse.ArgumentParser(description='IMMORTALS Jump starter')
parser.add_argument('-o', '--offline', action='store_true',
                    help='If set, the server will not send a "ready event to the test harness and fail due to no'
                         ' reply.  It will also send the "bootstrap" command to the Knowledge Repo.')
parser.add_argument('-t', '--log-to-terminal', action='store_true',
                    help='If set, logging for this script will be sent to the terminal. Otherwise, it will be '
                         'redirected to a file in the standard artifact location.')

config = get_configuration()

logging.basicConfig(level=logging.DEBUG)


def start_fuseki():
    global _fuseki_process, config

    env = os.environ.copy()

    fuseki_home = os.getenv('FUSEKI_HOME')

    if fuseki_home is None:
        fuseki_home = config.fuseki.root

    server_script = os.path.join(fuseki_home, 'fuseki-server')

    env['FUSEKI_HOME'] = fuseki_home
    env['FUSEKI_BASE'] = os.path.join(config.runtimeRoot, 'fuseki_base')
    env['FUSEKI_RUN'] = os.path.join(config.runtimeRoot, 'fuseki_run')

    port = config.fuseki.port

    stdout = os.path.join(config.artifactRoot, 'fuseki_stdout.txt')
    stderr = os.path.join(config.artifactRoot, 'fuseki_stderr.txt')

    with open(stdout, 'w') as f_stdout, open(stderr, 'w') as f_stderr:
        _fuseki_process = tpr.global_subprocess.Popen(
            ['bash', server_script, '--update', '--mem', '--port=' + str(port), '/ds'],
            env=env, stdout=f_stdout, stderr=f_stderr, stdin=subprocess.PIPE)

    logging.info('Fuseki is starting.... \n,'
                 'For stdout, please see "' + stdout + '". \n For stderr, please see "' + stderr + '".\n\n')

    tpr.sleep(4)


def start_repository_service():
    global _repository_service_process, config
    war = config.repositoryService.executableFile
    path = config.repositoryService.root

    port = config.repositoryService.port
    stdout = os.path.join(config.artifactRoot, 'repository_service_stdout.txt')
    stderr = os.path.join(config.artifactRoot, 'repository_service_stderr.txt')

    with open(stdout, 'w') as r_stdout, open(stderr, 'w') as r_stderr:
        _repository_service_process = tpr.global_subprocess.Popen(
            ['java', '-Djava.security.egd=file:/dev/urandom', '-Dserver.port=' + str(port), '-jar', war], cwd=path,
            stdout=r_stdout, stderr=r_stderr, stdin=subprocess.PIPE)

    logging.info('immortals-repository-service is starting.... \n,'
                 'For stdout, please see "' + stdout + '". \n For stderr, please see "' + stderr + '".\n\n')

    tpr.sleep(10)


def start_das_service():
    global _das_process, config

    stdout = os.path.join(config.artifactRoot, 'das_service_stdout.txt')
    stderr = os.path.join(config.artifactRoot, 'das_service_stderr.txt')

    with open(stdout, 'w') as r_stdout, open(stderr, 'w') as r_stderr:
        _das_process = tpr.global_subprocess.Popen(['java', '-jar',
                                                    config.dasService.executableFile,
                                                    get_configuration().immortalsRoot],
                                                   cwd=get_configuration().immortalsRoot, stdout=r_stdout,
                                                   stderr=r_stderr,
                                                   stdin=subprocess.PIPE)

    tpr.sleep(4)

    logging.info("DAS has started.  Press Ctrl-C to shut down.")


def start_olympus():
    ig.get_olympus()


def start_rest_endpoint():
    global config
    logging.info('Starting rest endpoint...')

    rest_server = ll_rest_endpoint.LLRestEndpoint()
    rest_server.start()
    tpr.sleep(2)
    logging.info('Ready to take submissions.')


def start_das_temp_endpoint():
    global config, _das_alt_endpoint_process
    logging.info("Starting temporary DAS endpoint")

    stdout = os.path.join(config.artifactRoot, 'das_tmp_stdout.txt')
    stderr = os.path.join(config.artifactRoot, 'das_tmp_stderr.txt')

    with open(stdout, 'w') as r_stdout, open(stderr, 'w') as r_stderr:
        _das_alt_endpoint_process = tpr.global_subprocess.Popen(
            ['java', '-jar', get_configuration().testAdapter.executableFile,
             '--websocket-port', str(get_configuration().dasService.websocketPort),
             '--auxillary-logging-port', str(get_configuration().testAdapter.port)
             ],

            cwd=get_configuration().immortalsRoot, stdout=r_stdout, stderr=r_stderr, stdin=subprocess.PIPE)

    tpr.sleep(2)

    logging.info("DAS tmp endpoint has been started.")


if __name__ == '__main__':

    ig.main_thread_cleanup_hookup()

    logging.Logger.setLevel(logging.getLogger(), logging.DEBUG if get_configuration().debugMode else logging.INFO)

    args = parser.parse_args()

    ig.get_event_router().subscribe_listener(EventType.ERROR, th_client.process_error)
    ig.get_event_router().subscribe_listener(EventType.STATUS, th_client.process_status)
    if os.path.exists(get_configuration().dataRoot) and not os.path.exists(config.artifactRoot):
        os.mkdir(config.artifactRoot)

    ig.get_event_router().set_log_events_to_file(EventType.NETWORK_ACTIVITY,
                                                 os.path.join(config.artifactRoot, 'network_activity_log.txt'))

    logging.getLogger().addHandler(
        logging.FileHandler(os.path.join(config.artifactRoot, 'start_log.txt')))

    if not args.log_to_terminal:
        ep = tpr.get_std_endpoint(config.artifactRoot, 'start')
        sys.stdout = ep.out
        sys.stderr = ep.err

    p0 = config.logFile
    p0 = p0[:(p0 if p0[len(p0) - 1:len(p0)] is not '/' else p0[:len(p0) - 1]).rfind('/')]

    p1 = config.dataRoot
    p1 = p1[:(p1 if p1[len(p1) - 1:len(p1)] is not '/' else p1[:len(p1) - 1]).rfind('/')]

    # start_rest_endpoint()

    if not os.path.exists(p0):
        ig.get_event_router().submit(EventTags.THErrorDasLogFile, 'The path \'' + p0 + '\' does not exist!')

    elif not os.path.exists(p1):
        ig.get_event_router().submit(EventTags.THErrorTestDataFile, 'The path \'' + p1 + '\' does not exist!')

    else:

        if not os.path.exists(config.runtimeRoot):
            os.mkdir(config.runtimeRoot)

        if not os.path.exists(config.resultRoot):
            os.mkdir(config.resultRoot)

        # start_fuseki()
        # start_repository_service()
        # start_das_service()
        start_das_temp_endpoint()

        # if args.offline:
        #     pass
        # 
        #     # url = 'http://localhost:9999/immortalsRepositoryService/bootstrap'
        #     # requests.post(url=url)
        # 
        #     # url = 'http://localhost:9999/immortalsRepositoryService/pushDeploymentModel'
        #     # headers = {'Accept': 'text/plain', 'Content-Type': 'text/plain'}
        #     # payload = open('/Users/austin/Documents/workspaces/primary/immortals/repo/'
        #     #                + 'immortals/models/scenario/deployment_model.ttl').read()
        #     # r = requests.post(url, headers=headers, data=payload)
        #     # print(r.content)
        # 
        # else:
        #     ig.get_event_router().submit(event_tag=EventTags.THSubmitReady, data=None)
        #     tpr.sleep(4)

        if tpr.keep_running():
            tpr.start_runtime_loop()
