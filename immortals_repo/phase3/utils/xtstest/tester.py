#!/usr/bin/env python3

import argparse
import atexit
import json
import os
import shutil
import subprocess
import time
from typing import List, Union, Dict

from lxml import etree
from lxml.etree import XSLTApplyError
from xmldiff.actions import UpdateAttrib
from xmldiff.main import diff_files

from immortals_utils import IMMORTALS_ROOT, TestScenario, get_s6_scenarios, \
    get_s6_all_custom_scenarios, get_s6_bbn_custom_scenario_names, get_s6_swri_custom_scenario_names, \
    get_s6_swri_predefined_scenario_names, KR_XTS_TESTER_JAR

scenarios = get_s6_scenarios()
scenario_names = list(scenarios.keys())
sorted(scenario_names)

NO_DIFF_EXPECTED_FILE_CONTENTS = 'NO_DIFF_EXPECTED-6f01c82c-2179-44be-81e2-d1710e0e15d5-NO_DIFF_EXPECTED'

IMMORTALS_DIFF_TOOL = 'meld' if 'IMMORTALS_DIFF_TOOL' not in os.environ else os.environ['IMMORTALS_DIFF_TOOL']

parser = argparse.ArgumentParser(description="XML Translation Service Tester",
                                 formatter_class=argparse.RawTextHelpFormatter)
parser.add_argument('--test', '-t', type=str, action='append', choices=scenario_names, metavar='',
                    help="The scenario name to test. Allowed Values: \n" + '\n'.join(scenario_names))
parser.add_argument('--test-all-bbn', '-b', action='store_true',
                    help='Tests all custom BBN tests')

parser.add_argument('--test-all-swri-custom', '-c', action='store_true',
                    help='Tests all custom SwRI tests')

parser.add_argument('--test-all-swri-predefined', '-p', action='store_true',
                    help="Tests all the predefined swrio scenarios")

parser.add_argument('--test-all-swri', '-s', action='store_true',
                    help='Tests all swri scenarios')

parser.add_argument('--show-all-xsd-diff-commands', '-d', action='store_true',
                    help='Outputs a list of diff commands for comparing the initial and target XSD files')

if 'XTS_ROOT_DIR' in os.environ:
    XTS_ROOT_DIR = os.environ['XTS_ROOT_DIR']
else:
    XTS_ROOT_DIR = os.path.join(IMMORTALS_ROOT, 'knowledge-repo', 'cp', 'cp3.1', 'xsd-tranlsation-service-aql', 'aql')


def make_diff_command(file1, file2):
    """
    :type file1: str
    :type file2: str
    :rtype: str
    """
    return IMMORTALS_DIFF_TOOL + ' "' + os.path.relpath(file1) + '" "' + os.path.relpath(file2) + '"'


class XsdTranslationService:
    _process = None

    @staticmethod
    def start():
        if XsdTranslationService._process is None or XsdTranslationService._process.returncode is not None:
            XsdTranslationService._process = subprocess.Popen(['python3', 'server.py'],
                                                              cwd=XTS_ROOT_DIR)
            time.sleep(4)

    @staticmethod
    @atexit.register
    def stop():
        p = XsdTranslationService._process
        if p is not None:
            if p.returncode is None:
                p.terminate()
                p.wait(2)
                if p.returncode is None:
                    p.kill()


def diff_xml_files(desired_filepath: str, actual_filepath: str):
    diff = diff_files(desired_filepath, actual_filepath)
    marked_for_removal = list()
    for difference in list(diff):
        if (isinstance(difference, UpdateAttrib) and
                difference.name == '{http://www.w3.org/2001/XMLSchema-instance}schemaLocation' and
                difference.node == '/*[1]'):
            marked_for_removal.append(difference)

    for difference in marked_for_removal:
        diff.remove(difference)

    if len(diff) > 0:
        return False

    return True


class ValidationError:
    def __init__(self, schema_error_log_entry):
        self.filename = schema_error_log_entry.filename
        self.file_line = schema_error_log_entry.line
        self.message = schema_error_log_entry.message
        self.error_type = schema_error_log_entry.type_name

    def to_dict(self):
        return {
            "filename": self.filename,
            "fileLine": self.file_line,
            "message": self.message,
            "errorType": self.error_type
        }


class ValidationResult:
    def __init__(self):
        self.errors = list()  # type: List[str]
        self.warnings = list()  # type: List[str]
        self.successes = list()  # type: List[str]


class XmlTester:
    def __init__(self, src_xsd: str, dst_xsd: str, src_xml_dir: str, target_result_dir: str, label: str,
                 validation_xml_dir=None):
        self.src_xsd = src_xsd
        self.dst_xsd = dst_xsd
        self.src_xml_dir = src_xml_dir
        self.target_result_dir = target_result_dir
        self.label = label
        self.transformed_xml_dir = os.path.join(self.target_result_dir, 'transformed_documents')
        os.mkdir(self.transformed_xml_dir)
        self._transformer_filepath = None
        self._transformer = None
        self.validation_errors = dict()  # type: Dict[str, List[ValidationError]]
        self.must_pass = True
        if validation_xml_dir is None:
            self.validation_xml_dir = None
            self.unchanged_filenames = dict()
        else:
            unchanged_filepath = os.path.join(validation_xml_dir, 'unchanged.json')
            if os.path.exists(unchanged_filepath):
                self.unchanged_filenames = json.load(open(unchanged_filepath))
                self.validation_xml_dir = os.path.join(self.target_result_dir, 'expected_documents')
                os.mkdir(self.validation_xml_dir)

                for xml_file in os.listdir(src_xml_dir):
                    if self.unchanged_filenames[xml_file]:
                        src_root = self.src_xml_dir
                    else:
                        src_root = validation_xml_dir

                    shutil.copy(os.path.join(src_root, xml_file), os.path.join(self.validation_xml_dir, xml_file))

            else:
                self.validation_xml_dir = validation_xml_dir

    def set_must_pass(self, must_pass: bool):
        self.must_pass = must_pass

    def add_validation_errors(self, schema_error_log):
        for error_instance in schema_error_log:
            ve = ValidationError(error_instance)
            key = ve.error_type + ': ' + ve.message
            if key not in self.validation_errors:
                self.validation_errors[key] = list()

            self.validation_errors[key].append(ve)

    def save_validation_errors(self):
        if len(self.validation_errors) > 0:
            save_val = dict()

            for key in self.validation_errors.keys():
                inner_val = list()
                save_val[key] = inner_val
                for error in self.validation_errors[key]:
                    inner_val.append({
                        "fileName": error.filename,
                        "fileLine": error.file_line,
                    })

            json.dump(save_val, open(os.path.join(self.target_result_dir, 'validation_errors.json'), 'w'), indent=4)

    def validate_docs(self, src_xsd: str, xml_files: Union[str, List[str]]) -> ValidationResult:
        validation_result = ValidationResult()

        if isinstance(xml_files, str):
            xml_files = list(map(lambda x: os.path.abspath(os.path.join(xml_files, x)), os.listdir(xml_files)))

        schema = etree.XMLSchema(etree.parse(src_xsd))
        for doc_path in xml_files:
            doc = etree.parse(doc_path)
            display_doc_path = os.path.relpath(doc_path)

            if schema.validate(doc):
                validation_result.successes.append('XML file "' + display_doc_path + "' Adheres to the target schema.")
            else:
                err = 'XML file "' + display_doc_path + '" does not adhere to the target schema!'
                validation_result.errors.append(err)
                self.add_validation_errors(schema.error_log)

            if self.validation_xml_dir is None:
                validation_result.warnings.append(
                    'XML file "' + display_doc_path +
                    '" has no documents to compare contents against so semantic integrity is indeterminate!')
            else:
                file_name = os.path.basename(doc_path)
                desired_file = os.path.join(self.validation_xml_dir, file_name)
                initial_file = os.path.join(self.src_xml_dir, os.path.basename(doc_path))

                if not os.path.exists(desired_file):
                    if file_name in self.unchanged_filenames and self.unchanged_filenames[file_name] == True:
                        if not diff_xml_files(initial_file, doc_path):
                            err = 'XML file "' + display_doc_path + '" does not match the expected unmodified state!'
                            validation_result.errors.append(err)
                        else:
                            validation_result.successes.append(
                                'XML file "' + display_doc_path + '" contents are unchanged as expected."')

                    else:
                        raise Exception("The filepath '" + desired_file + "' does not exist to validate the scenario!")

                else:
                    if not diff_xml_files(desired_file, doc_path):
                        err = 'XML file "' + display_doc_path + '" does not match the expected state!'
                        validation_result.errors.append(err)
                    else:
                        validation_result.successes.append(
                            'XML file "' + display_doc_path + '" Contents matched expectation."')

        return validation_result

    @staticmethod
    def produce_visual_diff_command(produced_xml_path: str, target_xml_path: str):
        pass

    def _transform_document(self, doc_name: str) -> Union[str, None]:
        src_xml_filepath = os.path.join(self.src_xml_dir, doc_name)
        src_xml_document = etree.parse(open(src_xml_filepath, 'r'))
        try:
            transformed_document = self._transformer(src_xml_document)
            transformed_filepath = os.path.join(self.transformed_xml_dir, doc_name)

            transformed_str = etree.tostring(transformed_document, pretty_print=True).decode()
            open(transformed_filepath, 'w').write(transformed_str)
            return None

        except XSLTApplyError as e:
            if 'XPath evaluation returned no result.' == str(e):
                return ('ERROR: No transformation occurred applying "' + os.path.relpath(self._transformer_filepath) +
                        '" to "' + os.path.relpath(src_xml_filepath) + '"!'
                        )
            else:
                raise e

    def test(self) -> str:
        XsdTranslationService.start()

        cmd = ['java',
               '-DclientUrl=http://127.0.0.1:8090/xsdsts',
               '-DsrcSchema=' + self.src_xsd,
               '-DdstSchema=' + self.dst_xsd,
               '-DsrcDocs=' + self.src_xml_dir,
               '-DresultsDir=' + os.path.join(self.target_result_dir, 'kr_tester_results'),
               '-jar', KR_XTS_TESTER_JAR
               ]

        process_result = subprocess.run(cmd, cwd=self.target_result_dir)

        return_code = process_result.returncode

        if return_code == 0:
            if not self.must_pass:
                return self.label + "\n\tTranslation service executed successfully. Skipping validation since it is a predefined scenario."

            self._transformer_filepath = os.path.join(self.target_result_dir, 'xsdts-client', '0', 'response.xslt')
            self._transformer = etree.XSLT(etree.XML(
                open(self._transformer_filepath, 'r').read().encode()))

            results = list()

            for doc_name in os.listdir(self.src_xml_dir):
                transform_result = self._transform_document(doc_name)

                if transform_result is not None:
                    vr = ValidationResult()
                    vr.errors.append(transform_result)
                    results.append(vr)
                else:
                    results.append(self.validate_docs(self.dst_xsd, [os.path.join(self.transformed_xml_dir, doc_name)]))

            final_result = ValidationResult()
            for result in results:  # type: ValidationResult
                final_result.successes.extend(result.successes)
                final_result.warnings.extend(result.warnings)
                final_result.errors.extend(result.errors)

            rval = self.label + '\n'

            for success in final_result.successes:
                rval = (rval + '\t' + success + '\n')

            if not (len(final_result.errors) == 0 and len(final_result.warnings) == 0):
                for warning in final_result.warnings:
                    rval = (rval + 'WARNING: ' + warning + '\n')
                for error in final_result.errors:
                    rval = (rval + 'ERROR: ' + error + '\n')

            if self.validation_xml_dir is not None:
                rval = (rval +
                        '\tXSDDelta: \n\t\t' + make_diff_command(self.src_xsd, self.dst_xsd) +
                        '\n\tXMLDelta: \n\t\t' + make_diff_command(self.src_xml_dir,
                                                                   self.transformed_xml_dir) +
                        '\n\tDesiredDelta: \n\t\t' + make_diff_command(self.transformed_xml_dir,
                                                                       self.validation_xml_dir)
                        )

            else:
                rval = (rval +
                        '\tXSDDelta: \n\t\t' + make_diff_command(self.src_xsd, self.dst_xsd) +
                        '\n\tXMLDelta: \n\t\t' + make_diff_command(self.src_xml_dir,
                                                                   self.transformed_xml_dir)
                        )

            self.save_validation_errors()
            return rval + '\n\n'

        else:
            print('CMD: [' + '\n'.join(cmd) + ']')
            raise Exception('translation service failed with return code ' + str(return_code) + '!')


class TestScenarioXmlTester:
    def __init__(self, test_scenario: TestScenario, result_parent_dir: str):
        self.test_scenario = test_scenario
        self.src_xsd = test_scenario.get_initial_xsd_path()
        self.dst_xsd = test_scenario.get_updated_xsd_path()
        self.xml_dir = test_scenario.get_src_document_dir()
        validation_xml_dir = test_scenario.get_validation_document_dir()

        self.test_files = list(map(lambda x: os.path.join(self.xml_dir, x), os.listdir(self.xml_dir)))

        self.result_dir = os.path.join(result_parent_dir, test_scenario.shortName)
        os.mkdir(self.result_dir)
        self.tester = XmlTester(self.src_xsd, self.dst_xsd, self.xml_dir, self.result_dir, test_scenario.shortName,
                                validation_xml_dir)
        if test_scenario.is_updated_xsd_predefined():
            self.tester.set_must_pass(False)

    def test(self) -> str:
        return self.tester.test()


def main():
    global scenarios
    args = parser.parse_args()

    test_names = args.test
    test_all_bbn = args.test_all_bbn
    test_all_swri = args.test_all_swri
    test_all_swri_custom = args.test_all_swri_custom
    test_all_swri_predefined = args.test_all_swri_predefined
    show_all_xsd_diff_commands = args.show_all_xsd_diff_commands

    if (test_names is None and not test_all_bbn and not test_all_swri and not test_all_swri_custom
            and not test_all_swri_predefined and not show_all_xsd_diff_commands):
        print("ERROR: No parameters provided!")
        parser.print_help()
        return (-1)

    if show_all_xsd_diff_commands:
        scenarios = get_s6_all_custom_scenarios()

        for scenario in scenarios:
            initial_xsd = scenario.get_initial_xsd_path()
            updated_xsd = scenario.get_updated_xsd_path()
            initial_docs = scenario.get_src_document_dir()
            desired_docs = scenario.get_validation_document_dir()

            line = 'XSDDiff: ' + scenario.shortName + '\n\t' + make_diff_command(initial_xsd, updated_xsd)
            if desired_docs is None:
                line = line + '\n\tInputDocs: ' + os.path.relpath(initial_docs)
            else:
                line = line + '\n\tInputDesiredDocs: ' + make_diff_command(initial_docs, desired_docs)

            print(line)

        exit(0)

    if test_names is None:
        test_names = list()

    if test_all_bbn:
        test_names.extend(get_s6_bbn_custom_scenario_names())

    if test_all_swri or test_all_swri_predefined:
        test_names.extend(get_s6_swri_predefined_scenario_names())

    if test_all_swri or test_all_swri_custom:
        test_names.extend(get_s6_swri_custom_scenario_names())

    cwd = os.path.abspath('TEST_RESULTS')
    if os.path.exists(cwd):
        os.rename(cwd, cwd + '-' + str(int(os.path.getctime(cwd))))
    os.mkdir(cwd)

    result = ''
    for test_scenario_name in test_names:
        tester = TestScenarioXmlTester(scenarios[test_scenario_name], cwd)
        result = result + tester.test()

    print("Results: ")
    print(result)


if __name__ == '__main__':
    main()
