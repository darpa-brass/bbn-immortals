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
import uuid
from enum import Enum
from logging import Logger
from typing import List, Dict, Union, Tuple
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

BBN_SCENARIO_VALIDATION_LISTING = os.path.join(INGEST_DIR, 'bbn-validation_scenarios.json')
BBN_XML_VALIDATION_RESULT_JSON = os.path.join(INGEST_DIR, 'bbn-xml-validation-result.json')
BBN_ODB_VALIDATION_RESULT_JSON = os.path.join(INGEST_DIR, 'bbn-odb-validation-result.json')
BBN_XML_JUNIT_RESULT_XML = os.path.join(INGEST_DIR, 'TEST-bbn-xml-validation-result.xml')
BBN_ODB_JUNIT_RESULT_XML = os.path.join(INGEST_DIR, 'TEST-bbn-odb-validation-result.xml')
SWRI_SCENARIO_VALIDATION_LISTING = os.path.join(INGEST_DIR, 'swri-validation_scenarios.json')
SWRI_XML_VALIDATION_RESULT_JSON = os.path.join(INGEST_DIR, 'swri-xml-validation-result.json')
SWRI_ODB_VALIDATION_RESULT_JSON = os.path.join(INGEST_DIR, 'swri-odb-validation-result.json')
SWRI_XML_JUNIT_RESULT_XML = os.path.join(INGEST_DIR, 'TEST-swri-xml-validation-result.xml')
SWRI_ODB_JUNIT_RESULT_XML = os.path.join(INGEST_DIR, 'TEST-swri-odb-validation-result.xml')

DSL_TEST_SCENARIOS_FOLDER = os.path.join(SD, 'dsltest/scenarios')
DSL_TEST_SCENARIOS_FILE = os.path.join(DSL_TEST_SCENARIOS_FOLDER, 'test_scenarios.json')
IMMORTALS_ROOT = os.path.realpath(os.path.join(SD, "../../"))
ARTIFACT_DIR = os.path.realpath(os.path.join(SD, "../DEFAULT_ARTIFACT_DIRECTORY"))
IODBS_DIR = os.path.realpath(os.path.join(SD, "../immortals-orientdb-server"))
IODBS_DB_BACKUP_TARGET_DIR = os.path.join(IODBS_DIR, 'src/main/resources/test_databases/')
DATABASE_DIR = os.path.join(IODBS_DIR, "databases")
IODBS_JAR = os.path.join(IODBS_DIR, 'immortals-orientdb-server.jar')

FCS_DIR = os.path.realpath(os.path.join(SD, '../flighttest-constraint-solver/'))
VALIDATOR_JAR = os.path.join(FCS_DIR, 'validator.jar')

BBN_TEST_LISTING = os.path.join(IODBS_DIR, 'src/main/resources/s5_bbn_scenarios.json')
SWRIX_TEST_LISTING = os.path.join(IODBS_DIR, 'src/main/resources/s5_swri_scenarios.json')

if 'IMMORTALS_CHALLENGE_PROBLEMS_ROOT' not in os.environ:
    logger.error('Please set the environment variable "IMMORTALS_CHALLENGE_PROBLEMS_ROOT"!')
    exit(CP_ROOT_ERROR_EXIT_CODE)

SWRI_EXAMPLE_ROOT = os.environ['IMMORTALS_CHALLENGE_PROBLEMS_ROOT']
if not os.path.isdir(SWRI_EXAMPLE_ROOT):
    logger.error('The "IMMORTALS_CHALLENGE_PROBLEMS_ROOT" value "' + SWRI_EXAMPLE_ROOT + '" is not a directory!')
    exit(CP_ROOT_ERROR_EXIT_CODE)

SWRI_EXAMPLE_SCAN_ROOT = os.path.join(SWRI_EXAMPLE_ROOT, 'Scenarios/FlightTesting/Scenario_5/Examples')

if not os.path.isdir(SWRI_EXAMPLE_ROOT):
    logger.error('The directory "' + SWRI_EXAMPLE_ROOT + '" does not exist!')
    exit(CP_ROOT_ERROR_EXIT_CODE)

parser = argparse.ArgumentParser(description='Example ingestion helper')

parser.add_argument('--regen-examples', '-r', action='store_true',
                    help="Regenerates examples if the source files have changed")
parser.add_argument('--validate-examples', '-v', action='store_true',
                    help='Scans for and validates examples')
parser.add_argument('--bbn', '-b', action='store_true',
                    help='Performs the operation on BBN authored scenarios')
parser.add_argument('--swri', '-s', action='store_true',
                    help='Performs the operation on SwRI authored scenarios.')
parser.add_argument('--debug', action='store_true', help='Enables debug logging')


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


def _resolve_file(filepath: str):
    if os.path.exists(filepath):
        return os.path.abspath(filepath)

    if not filepath.startswith('/'):
        candidate_filepath = os.path.join(SD, filepath)
        if os.path.exists(candidate_filepath):
            return candidate_filepath

        candidate_filepath = filepath.replace('${IMMORTALS_CHALLENGE_PROBLEMS_ROOT}', SWRI_EXAMPLE_ROOT)
        if os.path.exists(candidate_filepath):
            return candidate_filepath

        candidate_filepath = filepath.replace('${IMMORTALS_ROOT}', IMMORTALS_ROOT)
        if os.path.exists(candidate_filepath):
            return candidate_filepath

        candidate_filepath = os.path.join(IMMORTALS_ROOT, filepath)
        if os.path.exists(candidate_filepath):
            return candidate_filepath

        raise Exception(
            'could not find file "' + filepath + ' "locally, in the immortals root, or the challenge-problems root! !')


def _unresolve_file(filepath: str):
    if SWRI_EXAMPLE_ROOT in filepath:
        return filepath.replace(SWRI_EXAMPLE_ROOT, '${IMMORTALS_CHALLENGE_PROBLEMS_ROOT}')

    if IMMORTALS_ROOT in filepath:
        return filepath.replace(IMMORTALS_ROOT, '${IMMORTALS_ROOT}')

    if SD in filepath:
        return filepath.replace(SD, '')

    return filepath


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


class TestScenario:

    def __init__(self, shortName: str, prettyName: str, timeoutMS: int, xmlInventoryPath: str, xmlMdlrootInputPath: str,
                 ingestedXmlInventoryHash: str, ingestedXmlMdlrootInputHash: str, expectedStatusSequence: List[str],
                 scenarioType: str):
        self.xmlInventoryPath = _resolve_file(xmlInventoryPath)
        self.xmlMdlrootInputPath = _resolve_file(xmlMdlrootInputPath)

        self.shortName = shortName
        self.prettyName = prettyName
        self.timeoutMS = timeoutMS
        self.ingestedXmlInventoryHash = ingestedXmlInventoryHash
        self.ingestedXmlMdlrootInputHash = ingestedXmlMdlrootInputHash
        self.expectedStatusSequence = expectedStatusSequence
        self.scenarioType = scenarioType


class TestType(Enum):
    BbnTest = (BBN_TEST_LISTING, BBN_SCENARIO_VALIDATION_LISTING, BLACKLISTED_FILENAMES, DSL_TEST_SCENARIOS_FILE, None,
               BBN_XML_VALIDATION_RESULT_JSON, BBN_ODB_VALIDATION_RESULT_JSON, BBN_XML_JUNIT_RESULT_XML,
               BBN_ODB_JUNIT_RESULT_XML)
    SwriTest = (SWRIX_TEST_LISTING, SWRI_SCENARIO_VALIDATION_LISTING, BLACKLISTED_FILENAMES, DSL_TEST_SCENARIOS_FILE,
                SWRI_EXAMPLE_SCAN_ROOT, SWRI_XML_VALIDATION_RESULT_JSON, SWRI_ODB_VALIDATION_RESULT_JSON,
                SWRI_XML_JUNIT_RESULT_XML, SWRI_ODB_JUNIT_RESULT_XML)

    def __init__(self, scenario_listing: str, tmp_scenario_listing: str, blacklisted_filenames: str,
                 dsl_test_scenarios_file: str, example_scan_root: str, xml_validation_result_json: str,
                 odb_validation_result_json: str, xml_junit_result_xml: str, odb_junit_result_xml: str):
        self.scenario_listing = scenario_listing
        self.tmp_scenario_listing = tmp_scenario_listing
        self.blacklisted_filenames = blacklisted_filenames
        self.dsl_test_scenarios_file = dsl_test_scenarios_file
        self.example_scan_root = example_scan_root
        self.xml_validation_result_json = xml_validation_result_json
        self.odb_validation_result_json = odb_validation_result_json
        self.xml_junit_result_xml = xml_junit_result_xml
        self.odb_junit_result_xml = odb_junit_result_xml


class TestScenarioContainer:
    _bbn_tests = dict()  # type: Dict[str, TestScenario]
    _swri_tests = dict()  # type: Dict[str, TestScenario]
    _all_tests = dict()  # type: Dict[str, TestScenario]
    _test_type_map = {
        TestType.BbnTest: _bbn_tests,
        TestType.SwriTest: _swri_tests,
    }

    with open(BBN_TEST_LISTING, 'r') as f:
        bbn_test_json = json.load(f)
        for test_json in bbn_test_json['scenarios']:
            test = TestScenario(**test_json)
            _bbn_tests[test.shortName] = test
            _all_tests[test.shortName] = test

    with open(SWRIX_TEST_LISTING, 'r') as f:
        swri_test_json = json.load(f)
        for test_json in swri_test_json['scenarios']:
            test = TestScenario(**test_json)
            _swri_tests[test.shortName] = test
            _all_tests[test.shortName] = test

    @classmethod
    def _write(cls, testType: TestType, target_path: str, conserve_absolute_path: bool):
        test_identifiers = list(cls._test_type_map[testType].values())  # type: List[TestScenario]
        test_identifiers.sort(key=lambda s: s.shortName)

        scenarios = list()
        for test_scenario in test_identifiers:
            clone = copy.deepcopy(test_scenario.__dict__)
            if not conserve_absolute_path:
                clone['xmlInventoryPath'] = _unresolve_file(clone['xmlInventoryPath'])
                clone['xmlMdlrootInputPath'] = _unresolve_file(clone['xmlMdlrootInputPath'])
            scenarios.append(clone)

        output_value = {
            'scenarios': scenarios
        }

        json.dump(output_value, open(target_path, 'w'), indent=4)

    @classmethod
    def _write_scenarios(cls, example_paths: List[str], inventory_paths: List[str], testType: TestType,
                         local_tmp_file: bool):
        """
        :param example_paths: All examples available
        :param inventory_paths: All inventories available
        :param local_tmp_file: Whether or not to write them to the temporary file
        :return: The list of new or updated scenarios
        """
        new_scenarios = list()
        if testType == TestType.SwriTest:
            for example_path in example_paths:
                for inventory_path in inventory_paths:
                    identifier = cls._update_scenario(example_path, inventory_path, testType)
                    if identifier is not None:
                        new_scenarios.append(identifier)

        if local_tmp_file:
            cls._write(testType, testType.tmp_scenario_listing, True)
            return None if len(new_scenarios) == 0 else new_scenarios
        else:
            if len(new_scenarios) == 0:
                return None
            else:
                cls._write(testType, testType.scenario_listing, False)
                return new_scenarios

    @classmethod
    def save_tmp_scenario_updates(cls, example_paths: List[str], inventory_paths: List[str], testType: TestType) -> \
            Union[List[str], None]:
        return cls._write_scenarios(example_paths, inventory_paths, testType, True)

    @classmethod
    def save_scenario_updates(cls, example_paths: List[str], inventory_paths: List[str],
                              testType: TestType) -> Union[List[str], None]:
        return cls._write_scenarios(example_paths, inventory_paths, testType, False)

    @classmethod
    def update_scenario_hashes(cls, scenario_identifiers: List[str], testType: TestType,
                               convserve_config_absolute_paths: bool):
        for scenario_identifier in scenario_identifiers:
            scenario = cls.get(scenario_identifier)
            scenario.ingestedXmlMdlrootInputHash = hashlib.sha256(
                open(scenario.xmlMdlrootInputPath, 'r').read().encode()).hexdigest()
            scenario.ingestedXmlInventoryHash = hashlib.sha256(
                open(scenario.xmlInventoryPath, 'r').read().encode()).hexdigest()

        cls._write(testType, testType.scenario_listing, convserve_config_absolute_paths)

    @classmethod
    def _update_scenario(cls, example_path: str, inventory_path: str, testType: TestType) -> Union[str, None]:
        """
        :param example_path: The path of the example XML
        :param inventory_path:  the path of the inventory XML
        :return: The scenario identifier if it is new
        """

        if testType == TestType.BbnTest:
            raise Exception("Scenarios should be predefined for BBN tests and should never be updated!!")

        example_id = os.path.basename(os.path.dirname(example_path)).strip('Example_')
        inventory_id = os.path.basename(os.path.dirname(inventory_path)).strip('Inventory_')

        identifier = ('s5e' + (('0' + example_id) if len(example_id) == 1 else example_id) +
                      'i' + (('0' + inventory_id) if len(inventory_id) == 1 else inventory_id))

        if cls.contains(identifier):
            scenario = cls.get(identifier)
            scenario.xmlInventoryPath = inventory_path
            scenario.xmlMdlrootInputPath = example_path

        else:
            if testType != TestType.SwriTest:
                raise Exception("Non-Swri scenarios should be manually created in the scenario configuration file!")

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
            logger.info('Adding scenario for file "' + example_path.replace(testType.example_scan_root, '') + '".')
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
    def get_outdated_scenario_identifiers(cls, testType: TestType):
        rval = list()
        for scenario in cls._test_type_map[testType].values():
            example_hash = hashlib.sha256(open(scenario.xmlMdlrootInputPath, 'r').read().encode()).hexdigest()
            inventory_hash = hashlib.sha256(open(scenario.xmlInventoryPath, 'r').read().encode()).hexdigest()
            if (scenario.ingestedXmlMdlrootInputHash != example_hash or
                    scenario.ingestedXmlInventoryHash != inventory_hash or
                    not os.path.exists(os.path.join(IODBS_DB_BACKUP_TARGET_DIR, scenario.shortName + '-backup.zip'))):
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

        # if fileName is not None:
        #     if not ('Example_' in fileName or 'Inventory_' in fileName):
        #         logger.error('Expected a filepath containing "Example_" or "Inventory_"!')
        #         exit(CP_ROOT_ERROR_EXIT_CODE)

        if scenarioIdentifier is None:
            if fileName is not None:
                if 'Example_' in fileName:
                    self._scenarioIdentifier = 's5e' + os.path.basename(os.path.dirname(fileName)).strip('Example_')
                elif 'Inventory_' in fileName:
                    self._scenarioIdentifier = 's5i' + os.path.basename(os.path.dirname(fileName)).strip('Inventory_')
                else:
                    self._scenarioIdentifier = os.path.basename(fileName).replace('.xml', '')

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

    @staticmethod
    def from_filepath(filepath: str):
        results_dict = json.load(open(filepath, 'r'))
        return ValidationResultContainer(**results_dict)

    def write_to_xml_file(self, filepath: str):
        xml_results = self.to_junit_results()
        with open(filepath, 'w') as f:
            f.write(xml_results)
        logger.info('Wrote JUnit validation results to "' + filepath + '".')

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
        return ElementTree.tostring(testsuites_xml).decode()


class Ingester:
    def __init__(self, testType: TestType):
        self.testType = testType

    def gather_files(self) -> Tuple[List[str], List[str]]:
        if self.testType == TestType.BbnTest:
            examples = set()
            inventories = set()
            for test_name in TestScenarioContainer.bbn_test_names():
                container = TestScenarioContainer.get(test_name)
                examples.add(container.xmlMdlrootInputPath)
                inventories.add(container.xmlInventoryPath)

            return list(examples), list(inventories)

        elif self.testType == TestType.SwriTest:
            return self._gather_swri_files('Example_'), self._gather_swri_files('Inventory_')

        else:
            raise Exception('Unexpected TestType "' + self.testType.name + '"!')

    def _gather_swri_files(self, wildcard: str) -> List[str]:
        file_dirs = list(map(lambda x: os.path.join(self.testType.example_scan_root, x),
                             sorted(list(
                                 filter(lambda x: x.startswith(wildcard), os.listdir(self.testType.example_scan_root))
                             ))))

        result = list()

        for file_dir in file_dirs:
            files = list(filter(
                lambda x: x.startswith('BRASS_') and x.endswith(
                    '.xml') and x not in self.testType.blacklisted_filenames,
                os.listdir(file_dir)))

            if len(files) == 0:
                logger.error('No files matching the expression "BRASS_*.xml" found in "' + file_dir + '"!')
                exit(CP_ROOT_ERROR_EXIT_CODE)

            elif len(files) > 1:
                logger.error('Multiple files matching the expression "BRASS_*.xml" found in "' + file_dir + '"!')
                exit(CP_ROOT_ERROR_EXIT_CODE)

            result.append(os.path.join(file_dir, files[0]))

        return result

    def validate_xml_files(self, example_files: List[str], inventory_files: List[str]) -> ValidationResultContainer:
        logger.info("Validating provided XML files...")
        cmd = ['java', '-jar', VALIDATOR_JAR,
               '-O', self.testType.xml_validation_result_json,
               '--inventory-requirements',
               '--mdlroot-requirements',
               '--mdlroot-usage'
               ]
        cmd = cmd + example_files + inventory_files
        # _exec_cmd(cmd=cmd, cwd=SD, label='validator.jar')
        _exec_cmd(cmd=cmd, label='xml_validator_jar')

        logger.info("Finished validating XML files.")

        results = ValidationResultContainer.from_filepath(self.testType.xml_validation_result_json)
        results.write_to_xml_file(self.testType.xml_junit_result_xml)
        return results

    def validate_odb_scenarios(self, example_files: List[str], inventory_files: List[str]):
        logger.info("Validating OrientDB scenarios...")
        TestScenarioContainer.save_tmp_scenario_updates(example_files, inventory_files, self.testType)
        cmd = ['java', '-jar', VALIDATOR_JAR,
               '-O', self.testType.odb_validation_result_json,
               '--validate-scenarios-from-file', self.testType.tmp_scenario_listing,
               '--use-odb'
               ]
        _exec_cmd(cmd, label='odb_validator_jar')

        logger.info("Finished validating OrientDB scenarios.")

        results = ValidationResultContainer.from_filepath(self.testType.odb_validation_result_json)
        results.write_to_xml_file(self.testType.odb_junit_result_xml)
        return results

    def update_odb_scenarios(self, example_files: List[str], inventory_files: List[str]) -> Union[List[str], None]:
        logger.info('Updating the test scenarios in the validator java resource directory.')
        return TestScenarioContainer.save_scenario_updates(example_files, inventory_files, self.testType)

    def update_odb_backups(self, conserve_absolute_paths: bool):
        outdated_scenario_identifiers = TestScenarioContainer.get_outdated_scenario_identifiers(self.testType)

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
            TestScenarioContainer.update_scenario_hashes(outdated_scenario_identifiers, self.testType,
                                                         conserve_absolute_paths)

    def update_dsltest_sceanrios(self) -> Union[List[str], None]:
        changed_scenarios = list()

        test_scenarios_root = json.load(open(DSL_TEST_SCENARIOS_FILE, 'r'))
        regression_scenarios = test_scenarios_root['regression_scenarios']
        staging_scenarios = test_scenarios_root['staging_scenarios']
        debug_scenarios = test_scenarios_root['debug_scenarios']
        bad_scenarios = test_scenarios_root['bad_scenarios']

        if self.testType == TestType.SwriTest:
            example_identifiers = TestScenarioContainer.swri_test_names()
        elif self.testType == TestType.BbnTest:
            example_identifiers = TestScenarioContainer.bbn_test_names()
        else:
            raise Exception('Unexpected TestType "' + self.testType.name + '"!')

        for test_identifier in example_identifiers:
            scenario_data = TestScenarioContainer.get(test_identifier)
            request_file = os.path.join('scenarios', test_identifier, test_identifier + '-dsl-swap-request.json')
            inventory_file = os.path.join('scenarios', test_identifier, test_identifier + '-dsl-swap-inventory.json')
            target_folder = os.path.join(DSL_TEST_SCENARIOS_FOLDER, scenario_data.shortName)
            inventory_tgt_path = os.path.join(target_folder, test_identifier + '-dsl-swap-inventory.json')
            example_tgt_path = os.path.join(target_folder, test_identifier + '-dsl-swap-request.json')
            new_scenario = False

            if test_identifier in regression_scenarios:
                dsltest_data = regression_scenarios[test_identifier]
            elif test_identifier in staging_scenarios:
                dsltest_data = staging_scenarios[test_identifier]
            elif test_identifier in debug_scenarios:
                dsltest_data = debug_scenarios[test_identifier]
            elif test_identifier in bad_scenarios:
                dsltest_data = bad_scenarios[test_identifier]
            else:
                dsltest_data = {
                    "request_file": request_file,
                    "inventory_file": inventory_file,
                    "success_expected": True
                }
                staging_scenarios[test_identifier] = dsltest_data
                new_scenario = True

            if dsltest_data['request_file'] != request_file:
                raise Exception('Scenario "' + test_identifier + '" request_file path has changed from "' +
                                dsltest_data['request_file'] + '" to "' + request_file + '"!!')

            if dsltest_data['inventory_file'] != inventory_file:
                raise Exception('Scenario "' + test_identifier + '" inventory_file path has changed from "' +
                                dsltest_data['inventory_file'] + '" to "' + inventory_file + '"!!')

            if os.path.exists(example_tgt_path):
                request_file_hash = hashlib.sha256(open(example_tgt_path, 'r').read().encode()).hexdigest()
            else:
                request_file_hash = str(uuid.uuid4())
            if os.path.exists(inventory_tgt_path):
                inventory_file_hash = hashlib.sha256(open(inventory_tgt_path, 'r').read().encode()).hexdigest()
            else:
                inventory_file_hash = str(uuid.uuid4())

            if (('request_file_hash' not in dsltest_data or dsltest_data['request_file_hash'] != request_file_hash) or
                    ('inventory_file_hash' not in dsltest_data or dsltest_data[
                        'inventory_file_hash'] != inventory_file_hash)):
                if new_scenario:
                    logger.info('New scenario ' + test_identifier + ' introduced. Regenerating DSL example')
                else:
                    logger.info(
                        'Scenario ' + test_identifier + ' file hashes have changed. Regenerating DSL example')

                if not os.path.exists(target_folder):
                    os.mkdir(target_folder)

                logger.info('Validating OrientDB usage and creating DSL Interchange Files...')
                cmd = ['java', '-jar', VALIDATOR_JAR, '--use-odb', '--scenario', scenario_data.shortName]
                # _exec_cmd(cmd, cwd=IODBS_DIR, label='validator-jar')
                result_dir = _exec_cmd(cmd, label='dsltest_update_validator_jar')
                inventory_filename = test_identifier + '-validation-input-inventory.json'
                inventory_src_path = os.path.join(result_dir, inventory_filename)
                example_filename = test_identifier + '-validation-input-requirements.json'
                example_src_path = os.path.join(result_dir, example_filename)

                if not os.path.exists(inventory_src_path):
                    raise Exception('No request DSL JSON was produced for the scenario "' + test_identifier + '!')
                if not os.path.exists(example_src_path):
                    raise Exception('No inventory DSL JSON was produced for the scenario "' + test_identifier + '!')

                shutil.copy(inventory_src_path, inventory_tgt_path)
                shutil.copy(example_src_path, example_tgt_path)

                dsltest_data['request_file_hash'] = hashlib.sha256(
                    open(example_tgt_path, 'r').read().encode()).hexdigest()
                dsltest_data['inventory_file_hash'] = hashlib.sha256(
                    open(inventory_tgt_path, 'r').read().encode()).hexdigest()

                changed_scenarios.append(test_identifier)
        if len(changed_scenarios) > 0:
            write_val = {
                'regression_scenarios': regression_scenarios,
                'bad_scenarios': bad_scenarios,
                'staging_scenarios': staging_scenarios,
                'debug_scenarios': debug_scenarios
            }
            json.dump(write_val, open(DSL_TEST_SCENARIOS_FILE, 'w'), indent=4)
            return changed_scenarios

        return None

    def validate_examples(self):
        examples, inventories = self.gather_files()

        results = self.validate_xml_files(examples, inventories)  # type: ValidationResultContainer

        failed = False
        for result in results.results:
            if result.failure:
                failed = True
                break

        if failed:
            logger.error("Failures detected. Please resolve the failures for validation in OrientDB to occur.")
        else:
            self.validate_odb_scenarios(examples, inventories)

    def regen_examples(self):
        examples, inventories = self.gather_files()

        if self.testType == TestType.SwriTest:
            logger.info("Checking for new scenarios...")
            new_scenarios = self.update_odb_scenarios(examples, inventories)

            if new_scenarios is None or len(new_scenarios) == 0:
                logger.info("No new scenarios found.")
            else:
                build_odb()

        outdated_scenario_identifiers = TestScenarioContainer.get_outdated_scenario_identifiers(self.testType)
        if outdated_scenario_identifiers is not None and len(outdated_scenario_identifiers) > 0:
            print('The files for the following scenarios are new or have changed: [' + ', '.join(
                outdated_scenario_identifiers) + ']')
            self.update_odb_backups(False)
            build_odb()
            build_validator()
            self.validate_odb_scenarios(examples, inventories)
        self.update_dsltest_sceanrios()


def main():
    args = parser.parse_args()
    if args.debug:
        logging.basicConfig(level=logging.DEBUG, format='%(message)s')
    else:
        logging.basicConfig(level=logging.INFO, format='%(message)s')

    ingester = None

    if args.bbn:
        ingester = Ingester(TestType.BbnTest)
    elif args.swri:
        ingester = Ingester(TestType.SwriTest)
    else:
        print('Please specify which scenarios you would like to use!')
        parser.print_help()
        exit(CLI_ERROR_CODE)

    if args.validate_examples:
        ingester.validate_examples()
    elif args.regen_examples:
        ingester.regen_examples()
    else:
        print('Please specify the operation you would like to perform!')
        parser.print_help()
        exit(CLI_ERROR_CODE)


if __name__ == '__main__':
    main()
