#!/usr/bin/env python

import argparse
import os
import requests
import string
import subprocess
import sys

from infrastructure.immortals import scenariorunner
from infrastructure.immortals.utils import replace

import das

SAMPLE_DIR='das-service/sample_inputs/'

SAMPLE_VALUES = []

DAS_URL = 'http://127.0.0.1'
DAS_SERVICE_PORT = '8080'
CMD_SUBMIT_DAS_REQUEST = ('curl', '-H', 'Content-Type:application/json', '-X', 'POST', '--data-binary', '@$FILEPATH!', DAS_URL + ':' + DAS_SERVICE_PORT + '/bbn/das/deployment-model')


for json_file in os.listdir(SAMPLE_DIR):
    SAMPLE_VALUES.append(json_file.replace('.json', ''))

parser = argparse.ArgumentParser(description="IMMoRTALS DAS Submission Utility")

parser.add_argument('source', metavar='SOURCE', type=str, help='The source data to feed to the DAS. This may either be a file or one of the follwoing predefined sample configurations: \n' + string.join(SAMPLE_VALUES, '\n'))

resultAnalysisCommandGroup = parser.add_mutually_exclusive_group()
resultAnalysisCommandGroup.add_argument('-tl', '--test-location', action='store_true', help='Performs the basic location provider test')
resultAnalysisCommandGroup.add_argument('-da', '--dynamic-analysis', action='store_true', help='Performs dynamic analysis on the result of the submission.')

def main():
    args = parser.parse_args()

    if os.path.isfile(args.source):
        source = args.source

    elif os.path.isfile(os.path.join(SAMPLE_DIR, args.source + '.json')):
        source = os.path.join(SAMPLE_DIR, args.source + '.json')

    else:
        print '"' + source + '" is not a valid filepath or sample configuration.\n\n'
        parser.print_help()
        sys.exit()

    cmd = list(CMD_SUBMIT_DAS_REQUEST)
    cmd = replace(cmd, '$FILEPATH!', source)

    print 'Submitting request to DAS....'
    subprocess.check_output(cmd)

    if args.test_location:
        sr = scenariorunner.ScenarioRunner('client-test-location', True, True, True, True, False)
        sr.execute_scenario()

    elif args.dynamic_analysis:
        sr = scenariorunner.ScenarioRunner('droidscope', True, True, True, True, False)
        sr.execute_scenario()

    else:
        parser.print_help()


if __name__ == '__main__':
    main()
