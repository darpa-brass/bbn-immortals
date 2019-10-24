#!/usr/bin/env python3

import argparse
import atexit
import json
import os
import re
import shutil
import subprocess
from typing import List, Dict, Iterable, Union

import time
from immortals_utils import IMMORTALS_ROOT, TestScenario, get_s6_scenarios, \
    get_s6_all_custom_scenarios, get_s6_bbn_custom_scenario_names, get_s6_swri_custom_scenario_names, \
    get_s6_all_custom_scenarios_names, KR_XTS_TESTER_JAR, \
    get_s6_swri_predefined_scenario_names
from lxml import etree
from xmldiff.actions import UpdateAttrib
from xmldiff.main import diff_files

scenarios = get_s6_scenarios()
scenario_names = list(scenarios.keys())
sorted(scenario_names)

validation_exceptions = [
    # 'SCHEMAV_CVC_IDC'
]

VALIDATION_ERR_MSG_PREFIX_MATCHES = {
    ", attribute 'IDREF': '' is not a valid value of the atomic type 'xs:IDREF'.",
    ": This element is not expected.",
    ": '0' is not a valid value of the atomic type 'xs:positiveInteger'.",
    ": [facet 'enumeration'] The value 'Byte' is not an element of the set",
    ": [facet 'enumeration'] The value 'Other' is not an element of the set",
    ": Missing child element(s).",
    ": Element content is not allowed, because the type definition is simple.",
    ": Element content is not allowed, because the content type is empty.",
    ": The attribute 'IDREF' is required but missing.",
    " evaluate to a node.",
    ": Character content is not allowed, because the content type is empty.",
    ": Warning: No precomputed value available, the value was either invalid or something strange happend.",
    ", attribute 'IDREF': Warning: No precomputed value available, the value was either invalid or something strange happend.",
    ", attribute 'ID': The attribute 'ID' is not allowed.",
    ": 'Other' is not a valid value of the atomic type",
    ": No match found for key-sequence",
    ": The attribute",
    ": 'Byte' is not a valid value of the atomic type",
    ": Not all fields of key identity-constraint",
    ": '' is not a valid value of the atomic type ",
    ": [facet 'pattern'] The value '' is not accepted by the pattern",
    ": Duplicate key-sequence"
}

VALIDATION_ERR_MSG_PREFIX_TRANSLATIONS = {
    ": The attribute": "Missing Attribute",
    ": Not all fields of key identity-constraint ": "Not all fields of key identity-constraint evaluate to a node.",
    ": [facet 'pattern'] The value '' is not accepted by the pattern": ": [facet 'pattern'] The value '' is not accepted by the pattern.",
    ": Duplicate key-sequence": ": Duplicate key-sequence in unique identity-constraint.",
    ": No match found for key-sequence": "Expected IDREF value does not have a matching ID."
}

NO_DIFF_EXPECTED_FILE_CONTENTS = 'NO_DIFF_EXPECTED-6f01c82c-2179-44be-81e2-d1710e0e15d5-NO_DIFF_EXPECTED'

IMMORTALS_DIFF_TOOL = 'meld' if 'IMMORTALS_DIFF_TOOL' not in os.environ else os.environ['IMMORTALS_DIFF_TOOL']

SAXON_JAR_PATH = os.path.abspath('saxon/saxon9he.jar')

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
parser.add_argument('--use-lxml', '-l', action='store_true',
                    help='Uses lxml to process the XSLT instead of saxon. This does not work for all sceanrios!')

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


def jsonify_output(displayable_results: List[str]):
    try:
        result_length = len(displayable_results)
        top_dict = dict()
        curr_list = None
        curr_idx = 0

        while curr_idx < result_length:
            curr_value = displayable_results[curr_idx]
            if (curr_idx + 1) < result_length:
                next_value = displayable_results[curr_idx + 1]
            else:
                next_value = None

            if next_value is not None:
                if curr_value.startswith('\t\t\t') or next_value.startswith('\t\t\t'):
                    raise Exception("Pparsing of triple tabs not supported!")

                elif not curr_value.startswith('\t') and (
                        next_value.startswith('\t') or next_value.startswith('ERROR')):
                    curr_list = list()
                    top_dict[curr_value] = curr_list
                    curr_idx = curr_idx + 1

                elif curr_value.startswith('\t') and next_value.startswith('\t\t'):
                    curr_list.append(curr_value + '  ' + next_value)
                    curr_idx = curr_idx + 2

                elif ((curr_value.startswith('\t') or curr_value.startswith('ERROR')) and
                      (next_value.startswith('\t') or next_value.startswith('ERROR'))):
                    curr_list.append(curr_value)
                    curr_idx = curr_idx + 1

                elif curr_value == '\n':
                    curr_list = None
                    curr_idx = curr_idx + 1
                else:
                    raise Exception("Bad conversion logic!")

            elif curr_value.startswith('\t'):
                curr_list.append(curr_value)
                curr_idx = curr_idx + 1

            elif curr_value == '\n':
                curr_idx = curr_idx + 1

            else:
                raise Exception("Bad conversion logic!")

        return top_dict
    except Exception as e:
        print(e)
        return displayable_results


class ValidationError:
    def __init__(self, schema_error_log_entry):
        self.filename = schema_error_log_entry.filename
        self.file_line = schema_error_log_entry.line
        self.message = schema_error_log_entry.message
        self.error_type = schema_error_log_entry.type_name
        self.is_allowable_error = False

    def to_dict(self):
        return {
            "filename": self.filename,
            "file_line": self.file_line,
            "message": self.message,
            "error_type": self.error_type,
            "is_allowable_error": self.is_allowable_error
        }


class ResultantData:
    def __init__(self, identifier: str):
        self.identifier = identifier
        self.errors = list()  # type: List[str]
        self.warnings = list()  # type: List[str]
        self.info = list()  # type: List[str]
        self.validation_errors = dict()  # type: Dict[str, List[ValidationError]]
        self.difference_errors = dict()  # type: Dict[str, List[str]]

    @staticmethod
    def create_fine_grained_validation_error_metrics(
            result_data: Union[Iterable['ResultantData'], 'ResultantData']) -> Dict:
        if isinstance(result_data, ResultantData):
            result_data = [result_data]

        subcategory_dict = dict()

        total_errors = 0

        p = re.compile(r"Element \'{(?P<namespace>[a-zA-Z0-9.:/\-]*)\}(?P<label>\w*)\'(?P<remainder>.*)")

        for data in result_data:
            for ve_type in data.validation_errors.keys():
                error_msgs = list(map(lambda x: x.message, data.validation_errors[ve_type]))

                for msg in error_msgs:
                    result_dict = re.match(p, msg).groupdict()
                    sub_msg = result_dict['remainder']

                    match_found = False
                    for candidate in VALIDATION_ERR_MSG_PREFIX_MATCHES:
                        if sub_msg.startswith(candidate):
                            match_found = True
                            if candidate in VALIDATION_ERR_MSG_PREFIX_TRANSLATIONS:
                                candidate = VALIDATION_ERR_MSG_PREFIX_TRANSLATIONS[candidate]

                            label = ve_type + ' - ' + candidate
                            total_errors = total_errors + 1

                            if label in subcategory_dict:
                                subcategory_dict[label] = subcategory_dict[label] + 1
                            else:
                                subcategory_dict[label] = 1
                            break

                    if not match_found:
                        print('Could not find a candidate for sub "' + sub_msg + '"!')
                        print('Could not find a candidate for "' + msg + '"!')

        esp = dict()
        esc = dict()

        for key in subcategory_dict:
            esp[key] = subcategory_dict[key] / total_errors
            esc[key] = subcategory_dict[key]

        esp = {k: v for k, v in sorted(esp.items(), key=lambda x: x[1], reverse=True)}
        esc = {k: v for k, v in sorted(esc.items(), key=lambda x: x[1], reverse=True)}

        return {
            'errorSubtypePercentages': esp,
            'errorSubtypeCounts': esc
        }

    @staticmethod
    def produce_validation_error_metrics(result_data: Union[Iterable['ResultantData'], 'ResultantData']) -> Dict:
        if isinstance(result_data, ResultantData):
            result_data = [result_data]

        results = dict()

        etp = dict()
        etc = dict()

        total_validation_errors = 0
        error_type_count_dict = dict()

        for data in result_data:
            for error_id in data.validation_errors.keys():
                error_data_list = data.validation_errors[error_id]
                err_count = len(error_data_list)
                total_validation_errors = total_validation_errors + err_count

                if error_id in error_type_count_dict:
                    error_type_count_dict[error_id] = error_type_count_dict[error_id] + err_count
                else:
                    error_type_count_dict[error_id] = err_count

        results['totalErrors'] = total_validation_errors

        for key in error_type_count_dict.keys():
            etp[key] = error_type_count_dict[key] / total_validation_errors
            etc[key] = error_type_count_dict[key]

        etp = {k: v for k, v in sorted(etp.items(), key=lambda x: x[1], reverse=True)}
        etc = {k: v for k, v in sorted(etc.items(), key=lambda x: x[1], reverse=True)}
        results['errorTypePercentages'] = etp
        results['errorTypeCounts'] = etc
        subcategory_results = ResultantData.create_fine_grained_validation_error_metrics(result_data)
        results.update(subcategory_results)

        return results

    def to_dict(self):
        rval = {
            'identifier': self.identifier,
            'errors': self.errors,
            'warinings': self.warnings,
            'info': self.info
        }

        if len(self.validation_errors) > 0:
            ve_dict = dict()
            rval['validation_errors'] = ve_dict

            for err in self.validation_errors.keys():
                ve_list = list()
                ve_dict[err] = ve_list
                validation_error_list = self.validation_errors[err]

                for validation_error in validation_error_list:
                    ve_list.append(validation_error.to_dict())

        rval['difference_errors'] = self.difference_errors

        return rval


class ScenarioResultantDataSet:
    def __init__(self, identifier: str):
        self.identifier = identifier
        self.document_results = dict()  # type: Dict[str, ResultantData]
        self.display_data = list()

    def to_dict(self):
        raw_results_dict = dict()
        rval = {
            'raw_results': raw_results_dict
        }

        for value in self.document_results.keys():
            raw_result = self.document_results[value]
            raw_results_dict[value] = raw_result.to_dict()

        return rval


class CombinedResults:
    def __init__(self):
        self.raw_data = dict()  # type: Dict[str, ScenarioResultantDataSet]

    def create_digest(self, displayable_results: List[str] = None):
        CUSTOM_SCENARIO_NAMES = get_s6_all_custom_scenarios_names()
        PREDEFINED_SCENARIO_NAMES = get_s6_swri_predefined_scenario_names()

        scenario_metrics_dict = dict()

        custom_scenarios_validation_errors = list()
        predefined_scenario_validation_errors = list()
        all_scenarios_validation_errors = list()

        for scenario in self.raw_data.values():
            scenario_validation_results = scenario.document_results.values()
            all_scenarios_validation_errors.extend(scenario_validation_results)
            if scenario.identifier in PREDEFINED_SCENARIO_NAMES:
                predefined_scenario_validation_errors.extend(scenario_validation_results)
            if scenario.identifier in CUSTOM_SCENARIO_NAMES:
                custom_scenarios_validation_errors.extend(scenario_validation_results)

            scenario_metrics_dict[scenario.identifier] = ResultantData.produce_validation_error_metrics(
                scenario_validation_results)

        results_dict = dict()
        results_dict['allScenarios'] = ResultantData.produce_validation_error_metrics(all_scenarios_validation_errors)
        results_dict['customScenarios'] = ResultantData.produce_validation_error_metrics(
            custom_scenarios_validation_errors)
        results_dict['predefinedScenarios'] = ResultantData.produce_validation_error_metrics(
            predefined_scenario_validation_errors)

        if displayable_results is not None:
            results_dict['displayedResults'] = jsonify_output(displayable_results)

        results_dict['scenarioBreakdown'] = scenario_metrics_dict

        results_digest_path = os.path.join(os.path.abspath('TEST_RESULTS'), 'collated_results.json')
        json.dump(results_dict, open(results_digest_path, 'w'), indent=4)


class XmlTester:
    def __init__(self, src_xsd: str, dst_xsd: str, src_xml_dir: str, target_result_dir: str, label: str,
                 validation_xml_dir=None, use_lxml=False):
        self.src_xsd = src_xsd
        self.dst_xsd = dst_xsd
        self.src_xml_dir = src_xml_dir
        self.target_result_dir = target_result_dir
        self.label = label
        self.transformed_xml_dir = os.path.join(self.target_result_dir, 'transformed_documents')
        os.mkdir(self.transformed_xml_dir)
        self.predefined_mode = False
        self.use_lxml = use_lxml
        self.results = list()  # type: List[ResultantData]

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

    def set_predefined_mode(self, predefined_mode: bool):
        self.predefined_mode = predefined_mode

    def diff_xml_files(self, desired_filepath: str, actual_filepath: str) -> List[str]:
        print ("DesiredDoc: " + desired_filepath)
        print ("ActualDoc: " + actual_filepath)
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
            rval = list()
            for val in diff:
                rval.append(str(val))
            return rval

        return None

    def validate_xml_contents(self, results_data: ScenarioResultantDataSet = None) -> ScenarioResultantDataSet:
        if results_data is None:
            results_data = ScenarioResultantDataSet(self.label)

        raw_results = dict() if results_data.document_results is None else results_data.document_results

        xml_files = list(
            map(lambda x: os.path.abspath(os.path.join(self.transformed_xml_dir, x)),
                os.listdir(self.transformed_xml_dir)))

        for doc_path in xml_files:
            display_doc_path = os.path.relpath(doc_path)
            doc_name = os.path.basename(doc_path)
            if doc_name in raw_results:
                resultant_data = raw_results[doc_name]
            else:
                resultant_data = ResultantData(doc_name)
                raw_results[doc_name] = resultant_data

            if self.validation_xml_dir is None:
                resultant_data.warnings.append(
                    'XML file "' + display_doc_path +
                    '" has no documents to compare contents against so semantic integrity is indeterminate!')

            else:
                file_name = os.path.basename(doc_path)
                desired_file = os.path.join(self.validation_xml_dir, file_name)
                initial_file = os.path.join(self.src_xml_dir, os.path.basename(doc_path))

                if not os.path.exists(desired_file):
                    if file_name in self.unchanged_filenames and self.unchanged_filenames[file_name] is True:
                        diff_error_list = self.diff_xml_files(initial_file, doc_path)
                        if diff_error_list is not None:
                            err = 'XML file "' + display_doc_path + '" does not match the expected unmodified state!'
                            resultant_data.errors.append(err)

                            if doc_name in raw_results:
                                resultant_data = raw_results[doc_name]
                            else:
                                resultant_data = ResultantData(doc_name)
                                raw_results[doc_name] = resultant_data
                            resultant_data.difference_errors[os.path.basename(desired_file)] = diff_error_list

                        else:
                            resultant_data.info.append(
                                'XML file "' + display_doc_path + '" contents are unchanged as expected."')

                    else:
                        raise Exception("The filepath '" + desired_file + "' does not exist to validate the scenario!")

                else:
                    diff_error_list = self.diff_xml_files(desired_file, doc_path)
                    if diff_error_list is not None:
                        err = 'XML file "' + display_doc_path + '" does not match the expected state!'
                        resultant_data.errors.append(err)

                        if doc_name in raw_results:
                            resultant_data = raw_results[doc_name]
                        else:
                            resultant_data = ResultantData(doc_name)
                            raw_results[doc_name] = resultant_data
                        resultant_data.difference_errors[os.path.basename(desired_file)] = diff_error_list

                    else:
                        resultant_data.info.append(
                            'XML file "' + display_doc_path + '" Contents matched expectation."')

        return results_data

    def _produce_translation(self):
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

        if return_code != 0:
            print('CMD: [' + '\\ \n'.join(cmd) + ']')
            raise Exception('translation service failed with return code ' + str(return_code) + '!')

    def _transform(self):
        transformer_filepath = os.path.join(self.target_result_dir, 'xsdts-client', '0', 'response.xslt')

        if self.use_lxml:
            transformer = etree.XSLT(etree.XML(open(transformer_filepath, 'r').read().encode()))

            for doc_name in os.listdir(self.src_xml_dir):
                src_xml_filepath = os.path.join(self.src_xml_dir, doc_name)
                transformed_filepath = os.path.join(self.transformed_xml_dir, doc_name)

                src_xml_document = etree.parse(open(src_xml_filepath, 'r'))
                transformed_document = transformer(src_xml_document)

                transformed_str = etree.tostring(transformed_document, pretty_print=True).decode()
                open(transformed_filepath, 'w').write(transformed_str)

        else:
            saxon_processes = list()
            cmds = list()

            for doc_name in os.listdir(self.src_xml_dir):
                src_xml_filepath = os.path.join(self.src_xml_dir, doc_name)
                transformed_filepath = os.path.join(self.transformed_xml_dir, doc_name)

                cmd = ['java', '-jar', SAXON_JAR_PATH,
                       '-s:' + src_xml_filepath,
                       '-xsl:' + transformer_filepath,
                       '-o:' + transformed_filepath]
                cmds.append(cmd)
                saxon_processes.append(subprocess.Popen(cmd))

            for idx in range(len(saxon_processes)):
                process = saxon_processes[idx]
                process.wait()

                if process.returncode != 0:
                    print('CMD: [' + ' \\ \n'.join(cmds[idx]) + ']')
                    raise Exception("Saxon XLST translator had a return code of '" + str(process.returncode) + "'!")

    def _validate_schema_compliance(self, results: ScenarioResultantDataSet = None) -> ScenarioResultantDataSet:
        if results is None:
            results = ScenarioResultantDataSet(self.label)

        results_dict = results.document_results

        xml_files = list(
            map(lambda x: os.path.abspath(os.path.join(self.transformed_xml_dir, x)),
                os.listdir(self.transformed_xml_dir)))

        # Load the schema
        schema = etree.XMLSchema(etree.parse(self.dst_xsd))
        for doc_path in xml_files:
            display_doc_path = os.path.relpath(doc_path)
            doc_name = os.path.basename(doc_path)
            if doc_name in results_dict:
                resultant_data = results_dict[doc_name]
            else:
                resultant_data = ResultantData(doc_name)
                results_dict[doc_name] = resultant_data

            # For each doc in the source folder
            doc = etree.parse(doc_path)

            # If it validates, add a success
            if schema.validate(doc):
                resultant_data.info.append('XML file "' + display_doc_path + "' Adheres to the target schema.")
            else:
                is_valid_error = True

                for error_instance in schema.error_log:
                    # Otherwise, iterate through the errors
                    ve = ValidationError(error_instance)

                    if ve.error_type in resultant_data.validation_errors:
                        ve_list = resultant_data.validation_errors[ve.error_type]
                    else:
                        ve_list = list()
                        resultant_data.validation_errors[ve.error_type] = ve_list

                    ve_list.append(ve)

                    if ve.error_type in validation_exceptions:
                        # TODO: Investigate error message here
                        ve.is_allowable_error = True
                        is_valid_error = False

                if is_valid_error:
                    err = 'XML file "' + display_doc_path + '" does not adhere to the target schema!'
                    if self.predefined_mode:
                        resultant_data.warnings.append(err)
                    else:
                        resultant_data.errors.append(err)
                else:
                    resultant_data.warnings.append(
                        'XML file "' + display_doc_path + '" Contains an allowable validation error.')

        return results

    def test(self) -> ScenarioResultantDataSet:
        self._produce_translation()
        self._transform()
        validation_results = self._validate_schema_compliance()
        if not self.predefined_mode:
            self.validate_xml_contents(validation_results)

        json.dump(validation_results.to_dict(),
                  open(os.path.join(self.target_result_dir, 'validation_results.json'), 'w'), indent=4)
        #        if not self.must_pass:
        #            return self.label + "\n\tTranslation service executed successfully. Skipping validation since it is a predefined scenario."

        displayData = list()
        displayData.append(self.label)

        for doc_name in validation_results.document_results.keys():
            validation_data = validation_results.document_results[doc_name]

            for warning in validation_data.warnings:
                displayData.append('\tWARNING: ' + warning)
            for error in validation_data.errors:
                displayData.append('ERROR: ' + error)

            for info in validation_data.info:
                displayData.append('\t\t' + info)

        displayData.append('')

        if self.validation_xml_dir is not None:
            displayData.append('\tXSDDelta:')
            displayData.append('\t\t' + make_diff_command(self.src_xsd, self.dst_xsd))
            displayData.append('\tXMLDelta:')
            displayData.append('\t\t' + make_diff_command(self.src_xml_dir, self.transformed_xml_dir))
            displayData.append('\tDesiredDelta')
            displayData.append('\t\t' + make_diff_command(self.transformed_xml_dir, self.validation_xml_dir))

        else:
            displayData.append('\tXSDDelta:')
            displayData.append('\t\t' + make_diff_command(self.src_xsd, self.dst_xsd))
            displayData.append('\tXMLDelta:')
            displayData.append('\t\t' + make_diff_command(self.src_xml_dir, self.transformed_xml_dir))

        validation_results.display_data.extend(displayData)
        validation_results.display_data.append('\n')
        validation_results.display_data.append('\n')
        return validation_results


class TestScenarioXmlTester:
    def __init__(self, test_scenario: TestScenario, result_parent_dir: str, use_lxml: bool):
        self.test_scenario = test_scenario
        self.src_xsd = test_scenario.get_initial_xsd_path()
        self.dst_xsd = test_scenario.get_updated_xsd_path()
        self.xml_dir = test_scenario.get_src_document_dir()
        validation_xml_dir = test_scenario.get_validation_document_dir()

        self.test_files = list(map(lambda x: os.path.join(self.xml_dir, x), os.listdir(self.xml_dir)))

        self.result_dir = os.path.join(result_parent_dir, test_scenario.shortName)
        os.mkdir(self.result_dir)
        self.tester = XmlTester(self.src_xsd, self.dst_xsd, self.xml_dir, self.result_dir, test_scenario.shortName,
                                validation_xml_dir, use_lxml=use_lxml)
        if test_scenario.is_updated_xsd_predefined():
            self.tester.set_predefined_mode(True)

    def test(self) -> ScenarioResultantDataSet:
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

    if not args.use_lxml:
        if not os.path.exists(SAXON_JAR_PATH):
            raise Exception('Please use "get_saxon.sh" to get saxon prior to using the "--use-saxon" flag!')

    combined_results = CombinedResults()
    displayable_results = list()
    for test_scenario_name in test_names:
        tester = TestScenarioXmlTester(scenarios[test_scenario_name], cwd, use_lxml=args.use_lxml)
        result = tester.test()
        combined_results.raw_data[test_scenario_name] = result
        displayable_results.extend(result.display_data)

    combined_results.create_digest(displayable_results)

    print("Results: ")
    print('\n'.join(displayable_results))


if __name__ == '__main__':
    main()
