import json
import os
from enum import Enum

_SCRIPT_DIRECTORY = os.path.dirname(os.path.realpath(__file__))
IMMORTALS_ROOT = os.path.realpath(os.path.join(_SCRIPT_DIRECTORY, '../../../'))
SWRI_EXAMPLE_ROOT = os.environ['IMMORTALS_CHALLENGE_PROBLEMS_ROOT']
MDL_DOCUMENTS_ROOT = os.path.join(IMMORTALS_ROOT, 'knowledge-repo/cp/cp3.1/cp-ess-min/etc/')


class MDLVersionEnum(Enum):
    v0_8_7 = 7
    v0_8_8 = 8
    v0_8_9 = 9
    v0_8_10 = 10
    v0_8_11 = 11
    v0_8_12 = 12
    v0_8_13 = 13
    v0_8_14 = 14
    v0_8_16 = 16
    v0_8_17 = 17
    v0_8_19 = 19

    def get_document_dir(self):
        return os.path.join(MDL_DOCUMENTS_ROOT, 'messages/v' + str(self.value))

    def get_schema_doc(self):
        version = str(self.value)
        return os.path.join(MDL_DOCUMENTS_ROOT, 'schemas', 'v' + version, 'MDL_v0_8_' + version + '.xsd')


# noinspection PyPep8Naming
class TestScenario:

    def __init__(self, shortName, prettyName, scenarioType, timeoutMS, expectedStatusSequence, xmlInventoryPath=None,
                 xmlMdlrootInputPath=None, initialXsdVersion=None, updatedXsdVersion=None, updatedXsdInputPath=None,
                 expectedJsonOutputStructure=None, ingestedXmlInventoryHash=None, ingestedXmlMdlrootInputHash=None,
                 expectedDauSelections=None):
        """
        :type shortName:  str
        :type prettyName: str
        :type scenarioType: str
        :type timeoutMS: int
        :type expectedStatusSequence: list[str]
        :type xmlInventoryPath: str
        :type xmlMdlrootInputPath:  str
        :type initialXsdVersion: str
        :type updatedXsdVersion: str
        :type updatedXsdInputPath: str
        :type expectedJsonOutputStructure: dict
        :type ingestedXmlInventoryHash: str
        :type ingestedXmlMdlrootInputHash: str
        :type expectedDauSelections: list[list[str]]
        """
        self.shortName = shortName
        self.prettyName = prettyName
        self.scenarioType = scenarioType
        self.timeoutMS = timeoutMS
        self.dbName = 'IMMORTALS_' + shortName
        self.expectedStatusSequence = expectedStatusSequence
        self.xmlInventoryPath = None if xmlInventoryPath is None else resolve_file(xmlInventoryPath)
        self.xmlMdlrootInputPath = None if xmlMdlrootInputPath is None else resolve_file(xmlMdlrootInputPath)
        self.initialXsdVersion = initialXsdVersion
        self.updatedXsdVersion = updatedXsdVersion
        self.updatedXsdInputPath = None if updatedXsdInputPath is None else resolve_file(updatedXsdInputPath)
        self.expectedJsonOutputStructure = expectedJsonOutputStructure
        self.ingestedXmlInventoryHash = ingestedXmlInventoryHash
        self.ingestedXmlMdlrootInputHash = ingestedXmlMdlrootInputHash
        self.expectedDauSelections = expectedDauSelections

        if scenarioType == "Scenario5swri" or scenarioType == "Scenario5bbn":
            assert xmlInventoryPath is not None
            assert xmlMdlrootInputPath is not None
            assert initialXsdVersion is None
            assert updatedXsdVersion is None
            assert updatedXsdInputPath is None

        elif scenarioType == "Scenario6swri" or scenarioType == "Scenario6bbn":
            assert xmlInventoryPath is None
            assert xmlMdlrootInputPath is None
            assert initialXsdVersion is not None
            assert (updatedXsdVersion is not None or updatedXsdInputPath is not None)

    def get_initial_xsd_path(self):
        return MDLVersionEnum[self.initialXsdVersion.lower()].get_schema_doc()

    def get_updated_xsd_path(self):
        if self.updatedXsdVersion is not None:
            return MDLVersionEnum[self.updatedXsdVersion.lower()].get_schema_doc()
        elif self.updatedXsdInputPath is not None:
            return self.updatedXsdInputPath
        else:
            raise Exception('No updated schema version or updated schema document provided for "' +
                            self.shortName + '"!')

    def is_updated_xsd_predefined(self):
        return self.updatedXsdVersion is not None

    def get_src_document_dir(self):
        if (self.initialXsdVersion == 'V0_8_19' and self.updatedXsdInputPath is not None and
                self.scenarioType == 'Scenario6bbn'):
            xml_dir = os.path.realpath(os.path.join(
                os.path.dirname(self.updatedXsdInputPath), '../../base/messages/')
            )
            if not os.path.exists(xml_dir):
                raise Exception('No documents for scenario "' + self.shortName + "' found at '" + xml_dir + "'!")
            return xml_dir

        else:
            xml_dir = MDLVersionEnum[self.initialXsdVersion.lower()].get_document_dir()
            if not os.path.exists(xml_dir):
                raise Exception('No documents for scenario "' + self.shortName + "' found at '" + xml_dir + "'!")
            return xml_dir

    def get_validation_document_dir(self):
        if self.initialXsdVersion == 'V0_8_19' and self.updatedXsdInputPath is not None:
            if self.scenarioType == 'Scenario6bbn':
                validation_xml_dir = os.path.realpath(
                    os.path.join(os.path.dirname(self.updatedXsdInputPath), '../messages'))

                if not os.path.exists(validation_xml_dir):
                    raise Exception('Expected documents for validation of scenario "' + self.shortName +
                                    '" at "' + validation_xml_dir + '"!')
                return validation_xml_dir

            elif self.scenarioType == 'Scenario6swri':
                validation_xml_dir = os.path.realpath(
                    os.path.join(IMMORTALS_ROOT,
                                 'phase3/utils/bbn_test_scenarios/Scenario_6/swri_validation_docs',
                                 self.shortName, 'messages'))

                if not os.path.exists(validation_xml_dir):
                    raise Exception('Expected documents for validation of scenario "' + self.shortName +
                                    '" at "' + validation_xml_dir + '"!')
                return validation_xml_dir

        return None


def clean_json_lines(lines):
    """
    :type lines: list[str] or list[bytes]
    :rtype: list[str]
    """
    return_lines = list()
    for line in lines:
        if isinstance(line, bytes):
            line = line.decode()

        s_l = line.strip()
        if not s_l.startswith('//') and not s_l.startswith('#'):
            return_lines.append(s_l)

    return return_lines


def clean_json_str(s):
    """
    :type s: str or bytes
    :rtype: str
    """
    if isinstance(s, bytes):
        s = s.decode()
    stripped = s.strip()

    if '\n' in stripped:
        lines = stripped.split('\n')
        return ''.join(clean_json_lines(lines))


def resolve_file(filepath):
    """
    :type: filepath: str
    :rtype: str
    """
    if os.path.exists(filepath):
        return os.path.abspath(filepath)

    if not filepath.startswith('/'):
        candidate_filepath = os.path.join(_SCRIPT_DIRECTORY, filepath)
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


def _get_scenarios(source_files):
    """
    :type source_files: list[str]
    :rtype: dict[str, TestScenario]
    """

    scenarios = dict()
    for filepath in source_files:
        if not os.path.isabs(filepath):
            base_scenarios_path = os.path.abspath(os.path.join(
                IMMORTALS_ROOT, 'phase3/immortals-orientdb-server/src/main/resources/'))
            filepath = os.path.join(base_scenarios_path, filepath)

        json_data = json.loads(clean_json_str(open(filepath).read()))
        for scenario in json_data['scenarios']:
            scenarios[scenario['shortName']] = TestScenario(**scenario)

    return scenarios


def get_s6_swri_scenarios():
    """
    :rtype: dict[str, TestScenario]
    """
    return _get_scenarios(['s6_swri_scenarios.json'])


def get_s6_bbn_custom_scenario_names():
    """
    :rtype: list[str]
    """
    scenarios = _get_scenarios(['s6_bbn_scenarios.json']).values()

    return list(map(lambda x: x.shortName,
                    list(filter(lambda x: x.updatedXsdInputPath is not None, scenarios))
                    )
                )


def get_s6_swri_custom_scenario_names():
    """
    :rtype: list[str]
    """
    scenarios = _get_scenarios(['s6_swri_scenarios.json']).values()

    return list(map(lambda x: x.shortName,
                    list(filter(lambda x: x.updatedXsdInputPath is not None, scenarios))
                    )
                )


def get_s6_swri_predefined_scenario_names():
    """
    :rtype: list[str]
    """
    scenarios = _get_scenarios(['s6_swri_scenarios.json']).values()

    return list(map(lambda x: x.shortName,
                    list(filter(lambda x: x.updatedXsdVersion is not None, scenarios))
                    )
                )


def get_s6_all_custom_scenarios():
    """
    :rtype: list[TestScenario]
    """
    scenarios = list(_get_scenarios(['s6_bbn_scenarios.json']).values())
    scenarios.extend(list(_get_scenarios(['s6_swri_scenarios.json']).values()))
    return list(filter(lambda x: x.updatedXsdInputPath is not None, scenarios))


def get_s6_scenarios():
    return _get_scenarios(['s6_bbn_scenarios.json', 's6_swri_scenarios.json'])

# if __name__ == '__main__':
#     rval = list()
#     initial = None
#     updated = None
#     for value in MDLVersionEnum:
#         if updated is None and initial is None:
#             initial = value.name
#         elif updated is None:
#             updated = value.name
#         else:
#             initial = updated
#             updated = value.name
#
#         if initial is not None and updated is not None:
#             rval.append({
#                 "shortName": "s6_" + initial.replace('v0_8_', '') + 'to' + updated.replace('v0 _8_', ''),
#                 "scenarioType": "Scenario6bbn",
#                 "prettyName": "Scenario 6 - " + initial + " to " + updated,
#                 "timeoutMS": 600000,
#                 "initialXsdVersion": initial,
#                 "updatedXsdVersion": updated,
#                 "expectedStatusSequence": [
#                     "AdaptationSuccessful"
#                 ],
#                 "expectedJsonOutputStructure": {}
#             })
#
#     json.dump(rval, open('/home/awellman/Documents/workspaces/immortals/primary/git/immortals/phase3/immortals-orientdb-server/src/main/resources/s6_swri_scenarios_new.json', 'w'))
#
