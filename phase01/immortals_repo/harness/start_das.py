#!/usr/bin/env python

import os
from scenarioconductor.packages import subprocess32 as subprocess
import signal
import time

from scenarioconductor.configurationmanager import Configuration as config
from scenarioconductor.configurationmanager import IMMORTALS_ROOT
from scenarioconductor.server import Server
from scenarioconductor import threadprocessrouter as tpr
from scenarioconductor import immortalsglobals

RUNDIR = config.runtime_rootpath

fuseki_process = None
repository_service_process = None
das_process = None
rest_endpoint_process = None

rest_server = None


def exit_handler():
    print "Exit request detected. shutting down processes..."
    try:
        if rest_endpoint_process is not None:
            rest_endpoint_process.terminate()
    except:
        pass

    try:
        if das_process is not None:
            das_process.terminate()
    except:
        pass

    try:
        if repository_service_process is not None:
            repository_service_process.terminate()
    except:
        pass

    try:
        if fuseki_process is not None:
            fuseki_process.terminate()
    except:
        pass


def start_fuseki():
    server_script = os.path.join(config.fuseki.rootpath, 'fuseki-server')

    env = os.environ.copy()
    env['FUSEKI_HOME'] = config.fuseki.rootpath
    env['FUSEKI_BASE'] = os.path.join(RUNDIR, 'fuseki_base')
    env['FUSEKI_RUN'] = os.path.join(RUNDIR, 'fuseki_run')

    port = config.fuseki.port

    stdout = os.path.join(RUNDIR, 'fuseki_stdout.txt')
    stderr = os.path.join(RUNDIR, 'fuseki_stderr.txt')

    with open(stdout, 'w') as f_stdout, open(stderr, 'w') as f_stderr:
        fuseki_process = subprocess.Popen(['bash', server_script, '--update', '--mem', '--port=' + port, '/ds'],
                                          env=env, stdout=f_stdout, stderr=f_stderr, stdin=None)

    print 'Fuseki is starting.... \nFor stdout, please see "' + stdout + '". \n For stderr, please see "' + stderr + '".\n\n'

    time.sleep(4)


def start_repository_service():
    war = config.repository_service.executable_filepath
    path = config.repository_service.rootpath

    port = config.repository_service.port
    stdout = os.path.join(RUNDIR, 'repository_service_stdout.txt')
    stderr = os.path.join(RUNDIR, 'repository_service_stderr.txt')

    with open(stdout, 'w') as r_stdout, open(stderr, 'w') as r_stderr:
        repository_service_process = subprocess.Popen(['java', '-Dserver.port=' + port, '-jar', war], cwd=path,
                                                      stdout=r_stdout, stderr=r_stderr, stdin=None)

    print 'immortals-repository-service is starting.... \nFor stdout, please see "' + stdout + '". \n For stderr, please see "' + stderr + '".\n\n'

    time.sleep(10)


def start_das_service():
    path = config.das_service.rootpath
    jar = config.das_service.executable_filepath
    repo_root = config.target_source_rootpath

    das_process = subprocess.Popen(['java', '-jar', jar, repo_root], cwd=IMMORTALS_ROOT, stdin=None)

    time.sleep(4)

    print "DAS has started.  Press Ctrl-C to shut down."


def start_rest_endpoint():
    print 'Starting rest endpoint...'

    rest_server = Server('localhost', '55555')
    tpr.start_thread(Server.start, thread_args=[rest_server], shutdown_method=Server.shutdown, shutdown_args=[rest_server])

    time.sleep(2)
    print 'Ready to take submissions.'

if __name__ == '__main__':
    immortalsglobals.main_thread_cleanup_hookup()
    immortalsglobals.exit_handlers.append(exit_handler)

    if not os.path.exists(config.runtime_rootpath):
        os.mkdir(config.runtime_rootpath)

    if not os.path.exists(config.result_rootpath):
        os.mkdir(config.result_rootpath)

    start_fuseki()
    start_repository_service()
    start_das_service()
    start_rest_endpoint()
    signal.pause()
