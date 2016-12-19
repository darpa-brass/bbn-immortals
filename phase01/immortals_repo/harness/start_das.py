#!/usr/bin/env python

import argparse
import logging
import os
import sys

from scenarioconductor import immortalsglobals as ig
from scenarioconductor import ll_rest_endpoint
from scenarioconductor import threadprocessrouter as tpr
from scenarioconductor.reporting import testharnessreporter

_fuseki_process = None
_repository_service_process = None
_das_process = None
_rest_endpoint_process = None

_rest_server = None

parser = argparse.ArgumentParser(description='IMMORTALS Jumpstarter')
parser.add_argument('-o', '--offline', action='store_true',
                    help='If set, the server will log network transmissions to a file, but not actually attempt to send them.')
parser.add_argument('-t', '--log-to-terminal', action='store_true',
                    help='If set, logging for this script will be sent to the terminal. Otherwise, it will be redirected to a file in the standard artifact location.')
parser.add_argument('-thp', '--test-harness-port', type=int)
parser.add_argument('-tha', '--test-harness-address', type=str)
parser.add_argument('-tap', '--test-adapter-port', type=int)
parser.add_argument('-taa', '--test-adapter-address', type=str)


def start_fuseki():
    global _fuseki_process
    server_script = os.path.join(ig.configuration.fuseki.root, 'fuseki-server')

    env = os.environ.copy()
    env['FUSEKI_HOME'] = ig.configuration.fuseki.root
    env['FUSEKI_BASE'] = os.path.join(ig.configuration.runtimeRoot, 'fuseki_base')
    env['FUSEKI_RUN'] = os.path.join(ig.configuration.runtimeRoot, 'fuseki_run')

    port = ig.configuration.fuseki.port

    stdout = os.path.join(ig.configuration.artifactRoot, 'fuseki_stdout.txt')
    stderr = os.path.join(ig.configuration.artifactRoot, 'fuseki_stderr.txt')

    with open(stdout, 'w') as f_stdout, open(stderr, 'w') as f_stderr:
        _fuseki_process = tpr.Popen(['bash', server_script, '--update', '--mem', '--port=' + str(port), '/ds'],
                                    env=env, stdout=f_stdout, stderr=f_stderr, stdin=None)

    print 'Fuseki is starting.... \n,' \
          'For stdout, please see "' + stdout + '". \n For stderr, please see "' + stderr + '".\n\n'

    tpr.sleep(4)


def start_repository_service():
    global _repository_service_process
    war = ig.configuration.repositoryService.executableFile
    path = ig.configuration.repositoryService.root

    port = ig.configuration.repositoryService.port
    stdout = os.path.join(ig.configuration.artifactRoot, 'repository_service_stdout.txt')
    stderr = os.path.join(ig.configuration.artifactRoot, 'repository_service_stderr.txt')

    with open(stdout, 'w') as r_stdout, open(stderr, 'w') as r_stderr:
        _repository_service_process = tpr.Popen(['java', '-Djava.security.egd=file:/dev/urandom', '-Dserver.port=' + str(port), '-jar', war], cwd=path,
                                                stdout=r_stdout, stderr=r_stderr, stdin=None)

    print 'immortals-repository-service is starting.... \n,' \
          'For stdout, please see "' + stdout + '". \n For stderr, please see "' + stderr + '".\n\n'

    tpr.sleep(10)


def start_das_service():
    global _das_process

    stdout = os.path.join(ig.configuration.artifactRoot, 'das_service_stdout.txt')
    stderr = os.path.join(ig.configuration.artifactRoot, 'das_service_stderr.txt')

    with open(stdout, 'w') as r_stdout, open(stderr, 'w') as r_stderr:
        _das_process = tpr.Popen(['java', '-jar',
                                  ig.configuration.dasService.executableFile,
                                  ig.IMMORTALS_ROOT],
                                 cwd=ig.IMMORTALS_ROOT, stdout=r_stdout, stderr=r_stderr, stdin=None)

    tpr.sleep(4)

    print "DAS has started.  Press Ctrl-C to shut down."


def start_rest_endpoint():
    print 'Starting rest endpoint...'

    rest_server = ll_rest_endpoint.LLRestEndpoint(ig.configuration.testAdapter.url, ig.configuration.testAdapter.port)
    tpr.start_thread(thread_method=rest_server.start,
                     shutdown_method=rest_server.stop,
                     swallow_and_shutdown_on_exception=True)

    tpr.sleep(2)
    print 'Ready to take submissions.'


if __name__ == '__main__':
    ig.main_thread_cleanup_hookup()

    args = parser.parse_args()

    if args.test_harness_port is not None:
        ig.configuration.testHarness.port = args.test_harness_port

    if args.test_harness_address is not None:
        ig.configuration.testHarness.url = args.test_harness_address

    if args.test_adapter_port is not None:
        ig.configuration.testAdapter.port = args.test_adapter_port

    if args.test_adapter_address is not None:
        ig.configuration.testAdapter.url = args.test_adapter_address

    if args.offline:
        ig.set_logger(
            testharnessreporter.FakeTestHarnessReporter(ig.configuration.logRoot, ig.configuration.artifactRoot))

    else:
        ig.set_logger(testharnessreporter.TestHarnessReporter(ig.configuration.logRoot, ig.configuration.artifactRoot,
                                                              ig.configuration.testHarness))
        logging.getLogger().addHandler(
            logging.FileHandler(os.path.join(ig.configuration.artifactRoot, 'start_das_log.txt')))

    ig.failure_handlers.append(ig.logger().error_das)

    if not args.log_to_terminal:
        ep = tpr.get_std_endpoint(ig.configuration.artifactRoot, 'start_das')
        sys.stdout = ep.out
        sys.stderr = ep.err

    p0 = ig.configuration.logRoot
    p0 = p0[:(p0 if p0[len(p0) - 1:len(p0)] is not '/' else p0[:len(p0) - 1]).rfind('/')]

    p1 = ig.configuration.dataRoot
    p1 = p1[:(p1 if p1[len(p1) - 1:len(p1)] is not '/' else p1[:len(p1) - 1]).rfind('/')]

    if not os.path.exists(p0):
        ig.logger().error_das_log_file('The path \'' + p0 + '\' does not exist!')

    elif not os.path.exists(p1):
        ig.logger().error_test_data_file('The path \'' + p1 + '\' does not exist!')

    else:

        if not os.path.exists(ig.configuration.runtimeRoot):
            os.mkdir(ig.configuration.runtimeRoot)

        if not os.path.exists(ig.configuration.resultRoot):
            os.mkdir(ig.configuration.resultRoot)

        if not os.path.exists(ig.configuration.artifactRoot):
            os.mkdir(ig.configuration.artifactRoot)

        start_fuseki()
        start_repository_service()
        start_das_service()
        start_rest_endpoint()
        ig.logger().das_ready()

        tpr.sleep(4)

        if tpr.keep_running():
            tpr.start_runtime_loop()
