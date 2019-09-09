#!/usr/bin/env python3
import argparse
import copy
import hashlib
import json
import logging
import os
import shutil
import subprocess
import time
from logging import Logger
from typing import List, Dict, Union
from xml.etree import ElementTree

CP_ROOT_ERROR_EXIT_CODE = -66
BBN_ERROR_EXIT_CODE = -77
CLI_ERROR_CODE = -88

BLACKLISTED_FILENAMES = [
    'BRASS_Scenario5_Inventory1.xml'
]

logger = logging.getLogger("ingest.py")  # type: Logger

SD = os.path.dirname(os.path.realpath(__file__))

INGEST_DIR = os.path.join(SD, 'INGESTION_DATA')
if os.path.exists(INGEST_DIR):
    shutil.move(INGEST_DIR, os.path.join(SD, 'INGESTION_DATA-pre-' + str(int(time.time()))))
os.mkdir(INGEST_DIR)

SCENARIO_VALIDATION_LISTING = os.path.join(INGEST_DIR, 'validation_scenarios.json')
XML_VALIDATION_RESULT_JSON = os.path.join(INGEST_DIR, 'xml-validation-result.json')
ODB_VALIDATION_RESULT_JSON = os.path.join(INGEST_DIR, 'odb-validation-result.json')
XML_JUNIT_RESULT_XML = os.path.join(INGEST_DIR, 'TEST-xml-validation-result.xml')
ODB_JUNIT_RESULT_XML = os.path.join(INGEST_DIR, 'TEST-odb-validation-result.xml')

DSL_TEST_SCENARIOS_FOLDER = os.path.join(SD, 'dsltest/scenarios')
DSL_TEST_SCENARIOS_FILE = os.path.join(DSL_TEST_SCENARIOS_FOLDER, 'test_scenarios.json')
IMMORTALS_ROOT = os.path.realpath(os.path.join(SD, "../../"))
ARTIFACT_DIR = os.path.realpath(os.path.join(SD, "../DEFAULT_ARTIFACT_DIRECTORY"))
IODBS_DIR = os.path.realpath(os.path.join(SD, "../immortals-orientdb-server"))
IODBS_DB_BACKUP_TARGET_DIR = os.path.join(IODBS_DIR, 'src/main/resources/test_databases/')
IODBS_S5_SWRI_SCENARIO_FILE = os.path.join(IODBS_DIR, 'src/main/resources/s5_cp_scenarios.json')
DATABASE_DIR = os.path.join(IODBS_DIR, "databases")
IODBS_JAR = os.path.join(IODBS_DIR, 'immortals-orientdb-server.jar')

FCS_DIR = os.path.realpath(os.path.join(SD, '../flighttest-constraint-solver/'))
VALIDATOR_JAR = os.path.join(FCS_DIR, 'validator.jar')

BBN_TEST_LISTING = os.path.join(IODBS_DIR, 'src/main/resources/s5_bbn_scenarios.json')
SWRI_TEST_LISTING = os.path.join(IODBS_DIR, 'src/main/resources/s5_swri_scenarios.json')

if 'IMMORTALS_CHALLENGE_PROBLEMS_ROOT' not in os.environ:
    logger.error('Please set the environment variable "IMMORTALS_CHALLENGE_PROBLEMS_ROOT"!')
    exit(CP_ROOT_ERROR_EXIT_CODE)

CP_EXAMPLE_ROOT = os.environ['IMMORTALS_CHALLENGE_PROBLEMS_ROOT']
if not os.path.isdir(CP_EXAMPLE_ROOT):
    logger.error('The "IMMORTALS_CHALLENGE_PROBLEMS_ROOT" value "' + CP_EXAMPLE_ROOT + '" is not a directory!')
    exit(CP_ROOT_ERROR_EXIT_CODE)

CP_EXAMPLE_SCAN_ROOT = os.path.join(CP_EXAMPLE_ROOT, 'Scenarios/FlightTesting/Scenario_5/Examples')

if not os.path.isdir(CP_EXAMPLE_ROOT):
    logger.error('The directory "' + CP_EXAMPLE_ROOT + '" does not exist!')
    exit(CP_ROOT_ERROR_EXIT_CODE)

parser = argparse.ArgumentParser(description='Example ingestion helper')

parser.add_argument('--regen-swri-examples', '-r', action='store_true',
                    help="regenerates SwRI examples if the source files have changed")
parser.add_argument('--debug', action='store_true', help='Enables debug logging')
parser.add_argument('--validate-swri-examples', '-v', action='store_true',
                    help="Scans for and validates SwRI examples")


def parser_validator(args):
    if not (args.validate_swri_examples or args.regen_swri_examples):
        logger.error("No command provided!")
        parser.print_help()
        exit(CLI_ERROR_CODE)


def deconflict_dir(root_dir: str, dir_name: str):
    target_dir = os.path.join(root_dir, dir_name)
    if os.path.exists(target_dir) or os.path.exists(target_dir + '_0'):
        if os.path.exists(target_dir):
            shutil.move(target_dir, target_dir + '_0')
        base_dir = target_dir
        dir_idx = 1
        target_dir = base_dir + '_' + str(dir_idx)
        while os.path.exists(target_dir):
            dir_idx = dir_idx + 1
            target_dir = base_dir + '_' + str(dir_idx)
    os.mkdir(target_dir)
    return target_dir


def indent_xml(elem, level=0):
    i = "\n" + level * "  "
    j = "\n" + (level - 1) * "  "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "  "
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
        for subelem in elem:
            indent_xml(subelem, level + 1)
        if not elem.tail or not elem.tail.strip():
            elem.tail = j
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = j
    return elem


def _exec_cmd(cmd: List[str], label: str, cwd: str = None) -> str:
    """
    :param cmd: The command to execute
    :param cwd:  The directory to execute it in
    :param label: A label for the artifact directory
    :return: The artifact directory
    """
    target_dir = deconflict_dir(INGEST_DIR, label + '-ARTIFACTS')

    if cwd is None:
        cwd = target_dir
    if cmd[0] == 'java':
        cmd.insert(1, '-Dmil.darpa.immortals.artifactdirectory=' + target_dir)

    try:
        result = subprocess.run(cmd, cwd=cwd)
    except KeyboardInterrupt as e:
        logger.error('CWD: "' + str(cwd) + "'")
        logger.error('CMD: [' + ' '.join(cmd) + ']')
        raise e

    if result.returncode != 0:
        logger.error('CWD: "' + str(cwd) + "'")
        logger.error('CMD: [' + ' '.join(cmd) + ']')
        logger.error('Unexpected return code of ' + str(result.returncode) + '!')
        exit(result.returncode)

    return target_dir


class TestScenario:

    def __init__(self, shortName: str, prettyName: str, timeoutMS: int, xmlInventoryPath: str, xmlMdlrootInputPath: str,
                 ingestedXmlInventoryHash: str, ingestedXmlMdlrootInputHash: str, expectedStatusSequence: List[str],
                 scenarioType: str):

        self.xmlInventoryPath = xmlInventoryPath.replace('${IMMORTALS_CHALLENGE_PROBLEMS_ROOT}', CP_EXAMPLE_ROOT)
        self.xmlMdlrootInputPath = xmlMdlrootInputPath.replace('${IMMORTALS_CHALLENGE_PROBLEMS_ROOT}', CP_EXAMPLE_ROOT)
        if not os.path.exists(self.xmlInventoryPath) and not self.xmlInventoryPath.startswith('/'):
            self.xmlInventoryPath = os.path.join(IMMORTALS_ROOT, self.xmlInventoryPath)

        if not os.path.exists(self.xmlMdlrootInputPath) and not self.xmlMdlrootInputPath.startswith('/'):
            self.xmlMdlrootInputPath = os.path.join(IMMORTALS_ROOT, self.xmlMdlrootInputPath)

        if not os.path.exists(self.xmlInventoryPath):
            raise Exception('could not find inventory file at "' + xmlInventoryPath + '"!')
        if not os.path.exists(self.xmlMdlrootInputPath):
            raise Exception('could not find example file at "' + xmlMdlrootInputPath + '"!')

        self.shortName = shortName
        self.prettyName = prettyName
        self.timeoutMS = timeoutMS
        self.ingestedXmlInventoryHash = ingestedXmlInventoryHash
        self.ingestedXmlMdlrootInputHash = ingestedXmlMdlrootInputHash
        self.expectedStatusSequence = expectedStatusSequence
        self.scenarioType = scenarioType


class TestScenarioContainer:
    _bbn_tests = dict()  # type: Dict[str, TestScenario]
    _swri_tests = dict()  # type: Dict[str, TestScenario]
    _all_tests = dict()  # type: Dict[str, TestScenario]

    with open(BBN_TEST_LISTING, 'r') as f:
        bbn_test_json = json.load(f)
        for test_json in bbn_test_json['scenarios']:
            test = TestScenario(**test_json)
            _bbn_tests[test.shortName] = test
            _all_tests[test.shortName] = test

    with open(SWRI_TEST_LISTING, 'r') as f:
        swri_test_json = json.load(f)
        for test_json in swri_test_json['scenarios']:
            test = TestScenario(**test_json)
            _swri_tests[test.shortName] = test
            _all_tests[test.shortName] = test

    @classmethod
    def _write(cls, target_file: str):
        swri_tests = list(cls._swri_tests.values())  # type: List[TestScenario]
        swri_tests.sort(key=lambda s: s.shortName)

        scenarios = list()
        for test_scenario in swri_tests:
            clone = copy.deepcopy(test_scenario.__dict__)

            if clone['xmlInventoryPath'].startswith(IMMORTALS_ROOT):
                clone['xmlInventoryPath'] = clone['xmlInventoryPath'].replace(IMMORTALS_ROOT, '')
            clone['xmlInventoryPath'] = \
                clone['xmlInventoryPath'].replace(CP_EXAMPLE_ROOT, '${IMMORTALS_CHALLENGE_PROBLEMS_ROOT}')

            if clone['xmlMdlrootInputPath'].startswith(IMMORTALS_ROOT):
                clone['xmlMdlrootInputPath'] = clone['xmlMdlrootInputPath'].replace(IMMORTALS_ROOT, '')
            clone['xmlMdlrootInputPath'] = \
                clone['xmlMdlrootInputPath'].replace(CP_EXAMPLE_ROOT, '${IMMORTALS_CHALLENGE_PROBLEMS_ROOT}')
            scenarios.append(clone)

        output_value = {
            'scenarios': scenarios
        }

        json.dump(output_value, open(target_file, 'w'), indent=4)

    @classmethod
    def update_scenarios(cls, example_paths: List[str], inventory_paths: List[str]) -> Union[List[str], None]:
        """
        :param example_paths: All examples available
        :param inventory_paths: All inventories available
        :return: The list of new or updated scenarios
        """
        new_scenarios = list()
        for example_path in example_paths:
            for inventory_path in inventory_paths:
                identifier = cls._update_scenario(example_path, inventory_path)
                if identifier is not None:
                    new_scenarios.append(identifier)

        if len(new_scenarios) > 0:
            cls._write(SWRI_TEST_LISTING)
            return new_scenarios
        else:
            return None

    @classmethod
    def write_scenarios(cls, example_paths: List[str], inventory_paths: List[str], target_scenario_file: str) -> Union[
        List[str], None]:
        """
        :param example_paths: All examples available
        :param inventory_paths: All inventories available
        :param target_scenario_file: The target file to write the changes to
        :return: The list of new or updated scenarios
        """
        new_scenarios = list()
        for example_path in example_paths:
            for inventory_path in inventory_paths:
                identifier = cls._update_scenario(example_path, inventory_path)
                if identifier is not None:
                    new_scenarios.append(identifier)

        cls._write(target_scenario_file)
        if len(new_scenarios) == 0:
            return None
        else:
            return new_scenarios

    @classmethod
    def update_scenario_hashes(cls, scenario_identifiers: List[str]):
        for scenario_identifier in scenario_identifiers:
            scenario = cls.get(scenario_identifier)
            scenario.ingestedXmlMdlrootInputHash = hashlib.sha256(
                open(scenario.xmlMdlrootInputPath, 'r').read().encode()).hexdigest()
            scenario.ingestedXmlInventoryHash = hashlib.sha256(
                open(scenario.xmlInventoryPath, 'r').read().encode()).hexdigest()

        cls._write(SWRI_TEST_LISTING)

    @classmethod
    def _update_scenario(cls, example_path: str, inventory_path: str) -> str:
        """
        :param example_path: The path of the example XML
        :param inventory_path:  the path of the inventory XML
        :return: The scenario identifier if it is new
        """

        example_id = os.path.basename(os.path.dirname(example_path)).strip('Example_')
        inventory_id = os.path.basename(os.path.dirname(inventory_path)).strip('Inventory_')

        identifier = ('s5e' + (('0' + example_id) if len(example_id) == 1 else example_id) +
                      'i' + (('0' + inventory_id) if len(inventory_id) == 1 else inventory_id))

        if not cls.contains(identifier):
            test_scenario = TestScenario(
                shortName=identifier,
                prettyName='Scenario 5 SwRI Example ' + example_id + ' Inventory ' + inventory_id,
                timeoutMS=120000,
                xmlInventoryPath=inventory_path,
                xmlMdlrootInputPath=example_path,
                ingestedXmlInventoryHash='NOT_INGESTED',
                ingestedXmlMdlrootInputHash='NOT_INGESTED',
                expectedStatusSequence=[
                    "AdaptationSuccessful",
                    "AdaptationUnsuccessful"
                ],
                scenarioType="Scenario5swri"
            )
            logger.info('Adding scenario for file "' + example_path.replace(CP_EXAMPLE_ROOT, '') + '".')
            cls._swri_tests[identifier] = test_scenario
            cls._all_tests[identifier] = test_scenario
            return identifier

        return None

    @classmethod
    def all_test_names(cls) -> List[str]:
        return list(TestScenarioContainer._all_tests.keys())

    @classmethod
    def bbn_test_names(cls) -> List[str]:
        return list(TestScenarioContainer._bbn_tests.keys())

    @classmethod
    def swri_test_names(cls) -> List[str]:
        return list(TestScenarioContainer._swri_tests.keys())

    @classmethod
    def get(cls, scenario_name: str):
        return TestScenarioContainer._all_tests[scenario_name]

    @classmethod
    def contains(cls, scenario_name: str):
        return scenario_name in TestScenarioContainer._all_tests

    @classmethod
    def get_outdated_swri_scenario_identifiers(cls) -> List[str]:
        rval = list()
        for scenario in cls._swri_tests.values():
            example_hash = hashlib.sha256(open(scenario.xmlMdlrootInputPath, 'r').read().encode()).hexdigest()
            inventory_hash = hashlib.sha256(open(scenario.xmlInventoryPath, 'r').read().encode()).hexdigest()
            if (scenario.ingestedXmlMdlrootInputHash != example_hash or
                    scenario.ingestedXmlInventoryHash != inventory_hash):
                rval.append(scenario.shortName)

        return rval


class ValidationResult:
    def __init__(self, validationPerformed: str, failure: bool, details: str,
                 scenarioIdentifier: str = None, fileName: str = None, databaseName: str = None):

        self.validationPerformed = validationPerformed
        self.failure = failure
        self.details = details

        if databaseName is None and fileName is None:
            logger.error('No Filename or Database Name provided!')
            exit(BBN_ERROR_EXIT_CODE)

        if fileName is not None:
            if not fileName.startswith(CP_EXAMPLE_SCAN_ROOT):
                logger.error("Cannot do automated validation of examples outside of challenge-problems examples!")
                exit(BBN_ERROR_EXIT_CODE)

            if not ('Example_' in fileName or 'Inventory_' in fileName):
                logger.error('Expected a filepath containing "Example_" or "Inventory_"!')
                exit(CP_ROOT_ERROR_EXIT_CODE)

        if scenarioIdentifier is None:
            if fileName is not None:
                if 'Example_' in fileName:
                    self._scenarioIdentifier = 's5e' + os.path.basename(os.path.dirname(fileName)).strip('Example_')
                elif 'Inventory_' in fileName:
                    self._scenarioIdentifier = 's5i' + os.path.basename(os.path.dirname(fileName)).strip('Inventory_')

            else:
                self._scenarioIdentifier = databaseName[databaseName.rfind('/IMMORTALS_') + 11:]

        else:
            self._scenarioIdentifier = scenarioIdentifier

        self.fileName = fileName
        self.databaseName = databaseName

    @property
    def scenarioIdentifier(self):
        return self._scenarioIdentifier

    @property
    def sourceIdentifier(self):
        if self.fileName is not None:
            return self.fileName
        elif self.databaseName is not None:
            return self.databaseName
        else:
            return self.scenarioIdentifier


class ValidationResultContainer:
    def __init__(self, results: List):
        if len(results) > 0:
            if isinstance(results[0], Dict):
                self.results = list(
                    map(lambda x: ValidationResult(**x), results)
                )
            else:
                self.results = results

    def to_junit_results(self) -> str:
        testsuites_xml = ElementTree.Element("testsuites")
        testsuites_xml.set('id', 'testId')
        testsuites_xml.set('name', 'myName')
        testsuites_xml.set('tests', str(len(self.results)))
        testsuites_xml.set('failures', str(sum(t.failure for t in self.results)))
        testsuites_xml.set('time', '-1')

        testsuites_dict = dict()
        for result in self.results:
            if result.scenarioIdentifier in testsuites_dict:
                test_list = testsuites_dict[result.scenarioIdentifier]
            else:
                test_list = list()
                testsuites_dict[result.scenarioIdentifier] = test_list

            test_list.append(result)

        for testsuite_id in testsuites_dict.keys():
            testcases = testsuites_dict[testsuite_id]

            testsuite = ElementTree.SubElement(testsuites_xml, 'testsuite')
            testsuite.set('id', testsuite_id)
            testsuite.set('name', testcases[0].sourceIdentifier)
            testsuite.set('tests', str(len(testcases)))
            testsuite.set('failures', str(sum(t.failure for t in testcases)))
            testsuite.set('time', '-1')

            for testcase_obj in testcases:  # type: ValidationResult
                testcase = ElementTree.SubElement(testsuite, 'testcase')
                testcase.set('id', testcase_obj.validationPerformed)
                if testcase_obj.fileName is not None:
                    testcase.set('name', testcase_obj.fileName + ' - ' + testcase_obj.validationPerformed)
                elif testcase_obj.databaseName is not None:
                    testcase.set('name', testcase_obj.databaseName + ' - ' + testcase_obj.validationPerformed)
                else:
                    raise Exception('Could not find a name for the test scenario!')
                testcase.set('time', '-1')

                if testcase_obj.failure:
                    failure = ElementTree.SubElement(testcase, 'failure')
                    failure.set('message', testcase_obj.details)
                    failure.set('type', 'FAILURE')

        indent_xml(testsuites_xml)
        return ElementTree.tostring(testsuites_xml)


def gather_swri_example_files() -> List[str]:
    return _gather_swri_files('Example_')


def gather_swri_inventory_files() -> List[str]:
    return _gather_swri_files('Inventory_')


def _gather_swri_files(wildcard: str) -> List[str]:
    file_dirs = list(map(lambda x: os.path.join(CP_EXAMPLE_SCAN_ROOT, x),
                         sorted(list(
                             filter(lambda x: x.startswith(wildcard), os.listdir(CP_EXAMPLE_SCAN_ROOT))
                         ))))

    result = list()

    for file_dir in file_dirs:
        files = list(filter(
            lambda x: x.startswith('BRASS_') and x.endswith('.xml') and x not in BLACKLISTED_FILENAMES,
            os.listdir(file_dir)))

        if len(files) == 0:
            logger.error('No files matching the expression "BRASS_*.xml" found in "' + file_dir + '"!')
            exit(CP_ROOT_ERROR_EXIT_CODE)

        elif len(files) > 1:
            logger.error('Multiple files matching the expression "BRASS_*.xml" found in "' + file_dir + '"!')
            exit(CP_ROOT_ERROR_EXIT_CODE)

        result.append(os.path.join(file_dir, files[0]))

    return result


def validate_xml_files(example_files: List[str], inventory_files: List[str]) -> ValidationResultContainer:
    logger.info("Validating provided XML files...")
    cmd = ['java', '-jar', VALIDATOR_JAR,
           '-O', XML_VALIDATION_RESULT_JSON,
           '--inventory-requirements',
           '--mdlroot-requirements',
           '--mdlroot-usage'
           ]
    cmd = cmd + example_files + inventory_files
    # _exec_cmd(cmd=cmd, cwd=SD, label='validator.jar')
    _exec_cmd(cmd=cmd, label='xml_validator_jar')

    logger.info("Finished validating XML files.")

    results_dict = json.load(open(XML_VALIDATION_RESULT_JSON, 'r'))
    results = ValidationResultContainer(**results_dict)
    xml_results = results.to_junit_results()
    with open(XML_JUNIT_RESULT_XML, 'w') as f:
        f.write(xml_results.decode())
    logger.info('Wrote JUnit validation results to "' + XML_JUNIT_RESULT_XML + '".')
    return results


def validate_odb_scenarios(example_files: List[str], inventory_files: List[str]):
    logger.info("Validating OrientDB scenarios...")
    TestScenarioContainer.write_scenarios(example_files, inventory_files, SCENARIO_VALIDATION_LISTING)
    cmd = ['java', '-jar', VALIDATOR_JAR,
           '-O', ODB_VALIDATION_RESULT_JSON,
           '--validate-scenarios-from-file', SCENARIO_VALIDATION_LISTING,
           '--use-odb'
           ]
    _exec_cmd(cmd, label='odb_validator_jar')

    logger.info("Finished validating OrientDB scenarios.")

    results_dict = json.load(open(ODB_VALIDATION_RESULT_JSON, 'r'))
    results = ValidationResultContainer(**results_dict)
    xml_results = results.to_junit_results()
    with open(ODB_JUNIT_RESULT_XML, 'w') as f:
        f.write(xml_results.decode())
    logger.info('Wrote JUnit validation results to "' + ODB_JUNIT_RESULT_XML + '".')
    return results


def update_odb_scenarios(example_files: List[str], inventory_files: List[str]) -> Union[List[str], None]:
    logger.info('Updating the test scenarios in the validator java resource directory.')
    return TestScenarioContainer.update_scenarios(example_files, inventory_files)


def update_odb_backups():
    outdated_scenario_identifiers = TestScenarioContainer.get_outdated_swri_scenario_identifiers()

    if len(outdated_scenario_identifiers) <= 0:
        logger.info("ODB Backups are currently up to date.")
        return

    logger.info("Starting creation of OrientDB backups to be used during testing. This may take a while...")
    # Then build a command to regenerate all SwRI backup databases
    cmd = ['java', '-jar', IODBS_JAR, '--deployment-mode', 'BackupsWithUpdatedXml']
    for scenario in outdated_scenario_identifiers:
        cmd.append('--regen-scenario')
        cmd.append(scenario)

    # artifact_dir = _exec_cmd(cmd=cmd, cwd=IODBS_DIR, label='immortals-orientdb-server.jar')
    artifact_dir = _exec_cmd(cmd=cmd, label='update_backups_immortals-orientdb-server_jar')

    logger.info('Creation of backups finished.')

    for identifier in outdated_scenario_identifiers:
        filename = identifier + '-backup.zip'
        # Then, copy the produced backup databases to the database directory
        src = os.path.join(artifact_dir, 'PRODUCED_TEST_DATABASES', filename)
        tgt = os.path.join(IODBS_DB_BACKUP_TARGET_DIR, filename)
        logger.info('Updating backup file "' + filename + '".')
        if not os.path.isfile(src):
            raise Exception('The file "' + src + '" should have been created by the previous command!')

        if os.path.isfile(tgt):
            os.remove(tgt)
        shutil.copy(src, tgt)

        # Update the source XML hash in the database inventory
        TestScenarioContainer.update_scenario_hashes(outdated_scenario_identifiers)


def build_odb():
    logger.info('Rebuilding immortals-orientdb-server.jar...')
    cmd = ['bash', 'gradlew', 'clean', 'build', 'publish', '--quiet']
    _exec_cmd(cmd=cmd, cwd=IODBS_DIR, label='build_immortals-orientdb-server_jar')
    logger.debug('Finished rebuilding.')


def build_validator():
    logger.info('Rebuilding validator.jar...')
    cmd = ['bash', 'gradlew', 'clean', 'build', '--quiet', '-x', 'test']
    _exec_cmd(cmd=cmd, cwd=FCS_DIR, label='build_validator_jar')
    logger.debug('Finished rebuilding.')
    pass


def update_dsltest_sceanrios():
    with open(DSL_TEST_SCENARIOS_FILE, 'r') as f:
        test_scenarios_root = json.load(f)
        regression_scenarios = test_scenarios_root['regression_scenarios']
        staging_scenarios = test_scenarios_root['staging_scenarios']
        debug_scenarios = test_scenarios_root['debug_scenarios']

        swri_example_identifiers = TestScenarioContainer.swri_test_names()
        for test_identifier in swri_example_identifiers:
            scenario_data = TestScenarioContainer.get(test_identifier)
            request_file = scenario_data.xmlMdlrootInputPath
            inventory_file = scenario_data.xmlInventoryPath

            new_scenario = False

            if test_identifier in regression_scenarios:
                dsltest_data = regression_scenarios[test_identifier]
            elif test_identifier in staging_scenarios:
                dsltest_data = staging_scenarios[test_identifier]
            elif test_identifier in debug_scenarios:
                dsltest_data = debug_scenarios[test_identifier]
            else:
                dsltest_data = {
                    "request_file": request_file,
                    "request_file_hash": "",
                    "inventory_file": inventory_file,
                    "inventory_file_hash": ""
                }
                new_scenario = True
                if not os.path.exists(target_folder):
                    os.mkdir(target_folder)

            if dsltest_data['request_file'] != request_file:
                raise Exception('Scenario "' + test_identifier + '" request_file path has changed from "' +
                                dsltest_data['request_file'] + '" to "' + request_file + '"!!')

            if dsltest_data['inventory_file'] != inventory_file:
                raise Exception('Scenario "' + test_identifier + '" inventory_file path has changed from "' +
                                dsltest_data['inventory_file'] + '" to "' + inventory_file + '"!!')

            request_file_hash = hashlib.sha256(open(request_file, 'r').read().encode()).hexdigest()
            inventory_file_hash = hashlib.sha256(open(inventory_file, 'r').read().encode()).hexdigest()

            if (dsltest_data['request_file_hash'] != request_file_hash or
                    dsltest_data['inventory_file_hash'] != inventory_file_hash):
                if new_scenario:
                    logger.info('New scenario ' + test_identifier + ' introduced. Regenerating DSL example')
                else:
                    logger.info(
                        'Scenario ' + test_identifier + ' file hashes have changed. Regenerating DSL example')

                target_folder = os.path.join(DSL_TEST_SCENARIOS_FOLDER, scenario_data.shortName)

                logger.info('Validating OrientDB usage and creating DSL Interchange Files...')
                cmd = ['java', '-jar', VALIDATOR_JAR, '--use-odb', '--scenario', scenario_data.shortName]
                # _exec_cmd(cmd, cwd=IODBS_DIR, label='validator-jar')
                _exec_cmd(cmd, label='dsltest_update_validator_jar')


def validate_swri_examples():
    examples = gather_swri_example_files()
    inventories = gather_swri_inventory_files()

    results = validate_xml_files(examples, inventories)  # type: ValidationResultContainer
    result_list = results.results  # type: List[ValidationResult]

    failed = False
    for result in results.results:
        if result.failure:
            failed = True
            break

    if failed:
        logger.error("Failures detected. Please resolve the failures for validation in OrientDB to occur.")
    else:
        validate_odb_scenarios(examples, inventories)


def regen_swri_examples():
    examples = gather_swri_example_files()
    inventories = gather_swri_inventory_files()

    logger.info("Checking for new scenarios...")
    new_scenarios = update_odb_scenarios(examples, inventories)

    if new_scenarios is None or len(new_scenarios) == 0:
        logger.info("No new scenarios found.")
    else:
        build_odb()

    outdated_scenario_identifiers = TestScenarioContainer.get_outdated_swri_scenario_identifiers()
    if outdated_scenario_identifiers is not None and len(outdated_scenario_identifiers) > 0:
        print('The files for the following scenarios are new or have changed: [' + ', '.join(
            outdated_scenario_identifiers) + ']')
        update_odb_backups()
        build_odb()
        build_validator()
        validate_odb_scenarios(examples, inventories)


def main():
    args = parser.parse_args()
    if args.debug:
        logging.basicConfig(level=logging.DEBUG, format='%(message)s')
    else:
        logging.basicConfig(level=logging.INFO, format='%(message)s')

    parser_validator(args)

    if args.validate_swri_examples:
        validate_swri_examples()

    if args.regen_swri_examples:
        regen_swri_examples()


if __name__ == '__main__':
    main()
