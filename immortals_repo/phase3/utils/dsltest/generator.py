#!/usr/bin/env python3
import copy
import json
import os
import random as true_random
import shutil
import uuid
from enum import Enum
from typing import Optional, List, Dict, Tuple

from lxml import etree
from lxml.etree import Element

random = true_random.Random()
random.seed(6345928759)
uuid.uuid4 = lambda: uuid.UUID(int=random.getrandbits(128))

SCRIPT_DIRECTORY = os.path.dirname(os.path.realpath(__file__))

IMMORTALS_ROOT = os.path.realpath(os.path.join(SCRIPT_DIRECTORY, "../../../"))
SCENARIO_XML_TARGET_DIR = os.path.join(IMMORTALS_ROOT, 'phase3/utils/bbn_test_scenarios/Scenario_5/generated/')
SCENARIO_XML_LISTING_FILE = os.path.join(IMMORTALS_ROOT, 'phase3/immortals-orientdb-server/src/main/resources/s5_bbn_generated_scenarios.json')
SCENARIO_BACKUP_TARGET_DIR = os.path.join(IMMORTALS_ROOT, 'phase3/immortals-orientdb-server/src/main/resources/test_databases/generated/')
SWRI_EXAMPLE_ROOT = os.environ['IMMORTALS_CHALLENGE_PROBLEMS_ROOT']

ITERATIVE_TMP_FILE_PATH = '/tmp/immortals_tmp_xml_inventory.xml'

DATA_LENGTH_VARIANCE_PERCENTAGE = .30
SAMPLE_RATE_VARIANCE_PERCENTAGE = .30
VARIABLE_MODIFICATION_CHANCE = .45

PORT_TYPES = [
    'Analog', 'Bus', 'Ethernet', 'SignalConditioner', 'Serial', 'Thermocouple'
]


def _resolve_file(filepath: str):
    if os.path.exists(filepath):
        return os.path.abspath(filepath)

    if not filepath.startswith('/'):
        candidate_filepath = os.path.join(SCRIPT_DIRECTORY, filepath)
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
        newval = filepath.replace(IMMORTALS_ROOT, '')
        if newval.startswith('/'):
            return newval[1:]
        else:
            return newval

    if SCRIPT_DIRECTORY in filepath:
        return filepath.replace(SCRIPT_DIRECTORY, '')

    return filepath


def _write_xml_to_file(xml_str: str, filepath: str):
    # Redundant, but helps with debugging
    with open(filepath, 'w') as target:
        target.write(xml_str)

    parser = etree.XMLParser(remove_blank_text=True)
    tree = etree.fromstring(xml_str.encode(), parser)
    tree.getroottree().write(filepath, pretty_print=True)


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


# noinspection PyPep8Naming
class TestScenario:

    def __init__(self, shortName: str, prettyName: str, scenarioType: str, timeoutMS: int,
                 expectedStatusSequence: List[str], xmlInventoryPath: Optional[str] = None,
                 xmlMdlrootInputPath: Optional[str] = None, initialXsdVersion: str = None,
                 updatedXsdVersion: str = None, updatedXsdInputPath: str = None,
                 expectedJsonOutputStructure: Optional[Dict] = None,
                 ingestedXmlInventoryHash: Optional[str] = None,
                 ingestedXmlMdlrootInputHash: Optional[str] = None,
                 expectedDauSelections: Optional[List[List[str]]] = None
                 ):
        self.shortName = shortName
        self.prettyName = prettyName
        self.scenarioType = scenarioType
        self.timeoutMS = timeoutMS
        self.dbName = 'IMMORTALS_' + shortName
        self.expectedStatusSequence = expectedStatusSequence
        self.xmlInventoryPath = None if xmlInventoryPath is None else _resolve_file(xmlInventoryPath)
        self.xmlMdlrootInputPath = None if xmlMdlrootInputPath is None else _resolve_file(xmlMdlrootInputPath)
        self.initialXsdVersion = initialXsdVersion
        self.updatedXsdVersion = updatedXsdVersion
        self.updatedXsdInputPath = None if updatedXsdInputPath is None else _resolve_file(updatedXsdInputPath)
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

    def to_dict(self):
        rval = {
            'shortName': self.shortName,
            'prettyName': self.prettyName,
            'scenarioType': self.scenarioType,
            'timeoutMS': self.timeoutMS,
            'expectedStatusSequence': self.expectedStatusSequence,
        }

        if self.xmlInventoryPath is not None:
            rval['xmlInventoryPath'] = _unresolve_file(self.xmlInventoryPath)

        if self.xmlMdlrootInputPath is not None:
            rval['xmlMdlrootInputPath'] = _unresolve_file(self.xmlMdlrootInputPath)

        if self.initialXsdVersion is not None:
            rval['initialXsdVersion'] = self.initialXsdVersion

        if self.updatedXsdVersion is not None:
            rval['updatedXsdVersion'] = self.updatedXsdVersion

        if self.updatedXsdInputPath is not None:
            rval['updatedXsdInputPath'] = _unresolve_file(self.updatedXsdInputPath)

        if self.expectedJsonOutputStructure is not None:
            rval['expectedJsonOutputStructure'] = self.expectedJsonOutputStructure

        if self.ingestedXmlInventoryHash is not None:
            rval['ingestedXmlInventoryHash'] = self.ingestedXmlInventoryHash

        if self.ingestedXmlMdlrootInputHash is not None:
            rval['ingestedXmlMdlrootInputHash'] = self.ingestedXmlMdlrootInputHash

        if self.expectedDauSelections is not None:
            rval['expectedDauSelections'] = self.expectedDauSelections

        return rval


def get_scenarios() -> Dict[str, TestScenario]:
    scenarios = dict()

    base_scenarios_path = os.path.abspath(os.path.join(
        SCRIPT_DIRECTORY, '../../immortals-orientdb-server/src/main/resources/'))

    s5b_json = json.loads(clean_json_str(open(os.path.join(base_scenarios_path, 's5_bbn_scenarios.json')).read()))
    s5s_json = json.loads(clean_json_str(open(os.path.join(base_scenarios_path, 's5_swri_scenarios.json')).read()))

    for scenario in s5b_json['scenarios']:
        scenarios[scenario['shortName']] = TestScenario(**scenario)

    for scenario in s5s_json['scenarios']:
        scenarios[scenario['shortName']] = TestScenario(**scenario)

    return scenarios


class PortTransformation(Enum):
    AddMeasurementsToBeginning = 0,
    AddMeasurementsArbitrarily = 1,
    AddMeasurementsToEnd = 2,
    ChangePortType = 3
    # AddThermocoupleType = 4,
    # RemoveThermocoupleType = 5


class DauTransformation(Enum):
    DauSplitInTwoByModules = 5,
    DauSplitInTwoMixedModules = 6,


fake_thermocouples = ['G', 'W', 'Z', 'X', 'V', 'U']


class DauPortType:
    def __init__(self, xml_element: Element):
        self._xml_element = xml_element
        self.port_type = xml_element.text

        self.thermocouple = None if 'Thermocouple' not in xml_element.attrib else xml_element.attrib['Thermocouple']

    @property
    def xml(self) -> Element:
        return self._xml_element

    def clone_to_different_port_type(self) -> 'DauPortType':
        xml = copy.deepcopy(self._xml_element)

        new_port_type = random.choice(PORT_TYPES)
        while new_port_type == self.port_type:
            new_port_type = random.choice(PORT_TYPES)

        if new_port_type != 'Thermocouple' and self.port_type == 'Thermocouple' and self.thermocouple is not None:
            xml.attrib.pop('Thermocouple')

        xml.text = new_port_type

        return DauPortType(xml)


class DauMeasurement:
    def __init__(self, xml_element: Element):
        self._xml_element = xml_element
        self.sample_rate = int(xml_element.xpath('./SampleRate')[0].text)
        self.data_length = int(xml_element.xpath('./DataLength')[0].text)

    def to_string(self) -> str:
        return json.dumps({
            "DataLength": self.data_length,
            "DataRate": "@SampleRate * @DataLength",
            "SampleRate": self.sample_rate
        })

    @property
    def xml(self) -> Element:
        return self._xml_element

    @staticmethod
    def _gen_alt_value(current_value: int, deviation: float, increase: bool = None):
        if current_value == 1:
            return current_value

        if increase is None:
            minimum = max(1, (current_value - int(current_value * deviation)))
            maximum = current_value + int(current_value * deviation)

        elif increase:
            minimum = current_value
            maximum = current_value + int(current_value * deviation)

        else:
            minimum = max(1, (current_value - int(current_value * deviation)))
            maximum = current_value

        rval = random.randrange(minimum, maximum)
        # print(str(current_value) + " -> (" + str(minimum) + "," + str(maximum) + ") -> " + str(rval))
        return random.randrange(minimum, maximum)

    def clone_to_less_than(self) -> 'DauMeasurement':
        xml = copy.deepcopy(self._xml_element)
        xml.xpath('./DataLength')[0].text = str(DauMeasurement._gen_alt_value(self.data_length,
                                                                              DATA_LENGTH_VARIANCE_PERCENTAGE, False))
        xml.xpath('./SampleRate')[0].text = str(DauMeasurement._gen_alt_value(self.sample_rate,
                                                                              SAMPLE_RATE_VARIANCE_PERCENTAGE, False))
        return DauMeasurement(xml)

    def clone_to_greater_than(self) -> 'DauMeasurement':
        xml = copy.deepcopy(self._xml_element)
        xml.xpath('./DataLength')[0].text = str(DauMeasurement._gen_alt_value(self.data_length,
                                                                              DATA_LENGTH_VARIANCE_PERCENTAGE, True))
        xml.xpath('./SampleRate')[0].text = str(DauMeasurement._gen_alt_value(self.sample_rate,
                                                                              SAMPLE_RATE_VARIANCE_PERCENTAGE, True))
        return DauMeasurement(xml)

    def clone_to_arbitrary(self) -> 'DauMeasurement':
        xml = copy.deepcopy(self._xml_element)
        xml.xpath('./DataLength')[0].text = str(DauMeasurement._gen_alt_value(self.data_length,
                                                                              DATA_LENGTH_VARIANCE_PERCENTAGE, None))
        xml.xpath('./SampleRate')[0].text = str(DauMeasurement._gen_alt_value(self.sample_rate,
                                                                              SAMPLE_RATE_VARIANCE_PERCENTAGE, None))
        return DauMeasurement(xml)


class DauPort:
    def __init__(self, xml_element: Element):
        self._xml_element = xml_element
        self.initial_id = xml_element.attrib['ID']

        measurements_list = xml_element.xpath('./GenericParameter/Measurement')
        if len(measurements_list) > 0:
            self.measurements = list(map(lambda x: DauMeasurement(x), measurements_list))  # type: List[DauMeasurement]
            self.measurements = sorted(self.measurements, key=DauMeasurement.to_string)  # type: List[DauMeasurement]
        else:
            self.measurements = None

        portdirection_xpath = xml_element.xpath('./PortDirection')
        self.port_direction = None if len(portdirection_xpath) == 0 else portdirection_xpath[0].text  # type: str

        portpolarity_xpath = xml_element.xpath('./PortPolarity')
        self.port_polarity = None if len(portpolarity_xpath) == 0 else portpolarity_xpath[0].text  # type: str

        functionality_xpath = xml_element.xpath('./GenericParameter/BBNPortFunctionality')
        self.functionality = None if len(functionality_xpath) == 0 else functionality_xpath[0].text  # type: str

        porttype_xpath = xml_element.xpath('./GenericParameter/PortType')
        default_porttype_xpath = xml_element.xpath('./PortTypes/PortType')

        if len(porttype_xpath) > 0:
            self.port_types = list(map(lambda x: DauPortType(x), porttype_xpath))  # type: List[DauPortType]

        elif len(default_porttype_xpath) > 0:
            self.port_types = list(map(lambda x: DauPortType(x), default_porttype_xpath))  # type: List[DauPortType]
        else:
            self.port_types = None

    @property
    def xml(self) -> Element:
        return self._xml_element

    def clone(self) -> 'DauPort':
        return DauPort(copy.deepcopy(self._xml_element))

    def add_measurement_to_beginning(self):
        if self.measurements is not None:
            # print('Adding measurement to beginning of ' + self.initial_id + '.')
            base_measurement = self.measurements[0]  # type: DauMeasurement
            new_measurement = base_measurement.clone_to_less_than()
            self.measurements.insert(0, new_measurement)
            self._xml_element.xpath('./GenericParameter/Measurement')[0].getparent().insert(0, new_measurement.xml)
            # data_length = max(0, (base_measurement.data_length - random.choice(range(DATA_LENGTH_VARIANCE))))
            # sample_rate = max(0, (base_measurement.sample_rate - random.choice(range(SAMPLE_RATE_VARANCE))))
            # measurement_element = Element('Measurement')  # type: Element
            # data_length_element = Element('DataLength')  # type: Element
            # data_length_element.text = str(data_length)
            # measurement_element.append(data_length_element)
            # sample_rate_element = Element('SampleRate')
            # sample_rate_element.text = str(sample_rate)
            # sample_rate_element.append(sample_rate_element)
            # self.measurements.insert(0, measurement_element)

    def add_measurement_arbitrarily(self):
        if self.measurements is not None:
            # print('Adding arbitrary measurement to ' + self.initial_id + '.')
            base_measurement = self.measurements[0] if len(self.measurements) == 1 else (
                self.measurements[random.randrange(0, len(self.measurements))])
            new_measurement = base_measurement.clone_to_arbitrary()
            new_idx = random.randrange(0, len(self.measurements))
            self.measurements.insert(new_idx, new_measurement)
            self._xml_element.xpath('./GenericParameter/Measurement')[0].getparent().insert(new_idx,
                                                                                            new_measurement.xml)

    def add_measurement_to_end(self):
        if self.measurements is not None:
            # print('Adding measurement to end of ' + self.initial_id + '.')
            base_measurement = self.measurements[len(self.measurements) - 1]
            new_measurement = base_measurement.clone_to_greater_than()
            self.measurements.append(new_measurement)
            # self._xml_element.xpath('./Measurement').append(new_measurement.xml)
            self._xml_element.xpath('./GenericParameter/Measurement')[0].getparent().append(new_measurement.xml)

    def change_port_type(self):
        if self.port_types is not None:
            new_port_type = self.port_types[0].clone_to_different_port_type()
            self.port_types.clear()
            self.port_types.append(new_port_type)

            gp_port_type_list = self._xml_element.xpath('./GenericParameter/PortType')
            if len(gp_port_type_list) > 0:
                parent = gp_port_type_list[0].getparent()
                for old_port_type in gp_port_type_list:
                    parent.remove(old_port_type)
                parent.append(new_port_type.xml)

            else:
                port_type_list = self._xml_element.xpath('./PortTypes/PortType')
                if len(port_type_list) > 0:
                    parent = port_type_list[0].getparent()
                    parent.remove(port_type_list[0])
                    parent.append(new_port_type)


class DauModule:
    def __init__(self, xml_element: Element):
        self._xml_element = xml_element
        ports_xml = xml_element.xpath('./Ports/Port')
        self.ports = list(map(lambda x: DauPort(x), ports_xml))
        self.initial_id = xml_element.attrib['ID']
        functionality_xpath = xml_element.xpath('./GenericParameter/BBNModuleFunctionality')
        self.functionality = None if len(functionality_xpath) == 0 else functionality_xpath[0].text

    @property
    def xml(self) -> Element:
        return self._xml_element

    def _recreate_module(self, ports: List[DauPort]) -> 'DauModule':
        module_xml = copy.deepcopy(self._xml_element)
        module_xml_ports = module_xml.xpath('./Ports/Port')
        module_xml_ports.clear()
        for port in ports:
            port_xml = copy.deepcopy(port.xml)
            module_xml_ports.append(port_xml)

        return DauModule(module_xml)

    def split(self) -> Tuple['DauModule', 'DauModule']:
        moduleA_ports = list()
        moduleB_ports = list()

        if self.functionality == 'ThermocoupleConditioner':
            to_A = True
            for port in self.ports:
                if port.functionality == 'Thermocouple':
                    if to_A:
                        moduleA_ports.append(port)
                        to_A = False
                    else:
                        moduleB_ports.append(port)
                        to_A = True

                else:
                    moduleA_ports.append(port)
                    moduleB_ports.append(port)

        elif self.functionality == 'SignalConditioner':
            to_A = True
            for port in self.ports:
                if port.functionality == 'SignalConditioning':
                    if to_A:
                        moduleA_ports.append(port)
                        to_A = False
                    else:
                        moduleB_ports.append(port)
                        to_A = True

                else:
                    moduleA_ports.append(port)
                    moduleB_ports.append(port)

        else:
            for port in self.ports:
                moduleA_ports.append(port)
                moduleB_ports.append(port)

        moduleA = self._recreate_module(moduleA_ports)
        moduleB = self._recreate_module(moduleB_ports)

        return moduleA, moduleB


class Dau:
    def __init__(self, xml_element: Element):
        self._xml_element = xml_element
        modules_xml = xml_element.xpath('./InternalStructure/Modules/Module')
        self.modules = list(map(lambda x: DauModule(x), modules_xml))
        self.initial_id = xml_element.attrib['ID']

    @staticmethod
    def _clone_element(element: Element) -> Element:
        clone = copy.deepcopy(element)
        id_elements = clone.xpath('//*[@ID]')
        idref_elements = clone.xpath('//* [@IDREF]')

        for id_element in id_elements:
            element_id = id_element.xpath('./@ID')[0]
            new_id = element_id + '-' + str(uuid.uuid4())[-12:]
            for idref_element in idref_elements:  # type: Element
                if idref_element.xpath('./@IDREF')[0] == element_id:
                    idref_element.attrib['IDREF'] = new_id

            id_element.attrib['ID'] = new_id

        return clone

    @property
    def xml(self) -> Element:
        return self._xml_element

    def _recreate_dau(self, modules: List[DauModule]):
        dau_xml = copy.deepcopy(self._xml_element)
        dau_xml_modules = dau_xml.xpath('./InternalStructure/Modules')[0]
        dau_xml_modules.clear()
        for module in modules:
            module_xml = copy.deepcopy(module.xml)
            dau_xml_modules.append(module_xml)

        dau_xml = Dau._clone_element(dau_xml)
        return Dau(dau_xml)

    def clone(self) -> 'Dau':
        return Dau(Dau._clone_element(self.xml))

    def split_by_modules(self) -> Tuple['Dau', 'Dau']:
        thermocouple_conditioner_modules = list()
        signal_conditioner_modules = list()

        for module in self.modules:
            if module.functionality == 'ThermocoupleConditioner':
                thermocouple_conditioner_modules.append(module)
            elif module.functionality == 'SignalConditioner':
                signal_conditioner_modules.append(module)
            else:
                thermocouple_conditioner_modules.append(module)
                signal_conditioner_modules.append(module)

        dauA = self._recreate_dau(thermocouple_conditioner_modules)
        dauB = self._recreate_dau(signal_conditioner_modules)
        return dauA, dauB

    def split_across_modules(self) -> Tuple['Dau', 'Dau']:
        dauA_modules = list()
        dauB_modules = list()

        for module in self.modules:
            moduleA, moduleB = module.split()
            dauA_modules.append(moduleA)
            dauB_modules.append(moduleB)

        dauA = self._recreate_dau(dauA_modules)
        dauB = self._recreate_dau(dauB_modules)

        return dauA, dauB


class InventoryExpander:

    def __init__(self, inventory_file: str, matching_daus: List[str], target_file: str):
        self.inventory_file = inventory_file
        self.matching_daus = matching_daus
        self.target_file = target_file

    def transform_matching_dau(self, dau_transformation: DauTransformation,
                               port_transformations: List[PortTransformation] = None,
                               port_modification_chance: int = 1) -> List[str]:
        return self._modify_or_add_dau_variation(True, dau_transformation, port_transformations,
                                                 port_modification_chance)

    def add_matching_dau_variation(self, dau_transformation: DauTransformation = None,
                                   port_transformations: List[PortTransformation] = None,
                                   port_modification_chance: int = 1) -> List[str]:
        return self._modify_or_add_dau_variation(False, dau_transformation, port_transformations,
                                                 port_modification_chance)

    def _modify_or_add_dau_variation(self, modify_in_place: bool, dau_transformation: DauTransformation = None,
                                     port_transformations: List[PortTransformation] = None,
                                     port_modification_chance: int = 1) -> List[str]:
        rval = list()

        doc = etree.parse(self.inventory_file)
        root = doc.getroot()
        dau_inventory = root.xpath("/DAUInventory")[0]
        xpath = "./NetworkNode[@ID='" + "' or @ID='".join(self.matching_daus) + "']"
        matching_daus_xml = dau_inventory.xpath(xpath)

        transformation_daus = list()

        for raw_dau_xml in matching_daus_xml:
            if dau_transformation is None:
                if modify_in_place:
                    dau = Dau(raw_dau_xml)
                else:
                    dau = Dau(raw_dau_xml).clone()
                    rval.append(dau.initial_id)
                    dau_inventory.append(dau.xml)

                transformation_daus.append(dau)
            else:
                dau = Dau(raw_dau_xml)

                if dau_transformation == DauTransformation.DauSplitInTwoMixedModules:
                    dauA, dauB = dau.split_across_modules()
                    transformation_daus.append(dauA)
                    transformation_daus.append(dauB)
                    dau_inventory.append(dauA.xml)
                    dau_inventory.append(dauB.xml)
                    rval.append(dauA.initial_id)
                    rval.append(dauB.initial_id)

                elif dau_transformation == DauTransformation.DauSplitInTwoByModules:
                    dauA, dauB = dau.split_by_modules()
                    transformation_daus.append(dauA)
                    transformation_daus.append(dauB)
                    dau_inventory.append(dauA.xml)
                    dau_inventory.append(dauB.xml)
                    rval.append(dauA.initial_id)
                    rval.append(dauB.initial_id)

                else:
                    raise Exception('Unexpected DAU Transformation "' + dau_transformation.name + '"!')

                if modify_in_place:
                    dau_inventory.remove(raw_dau_xml)

        if port_transformations is not None:
            for dau in transformation_daus:
                for transformation in port_transformations:
                    if transformation == PortTransformation.AddMeasurementsToBeginning:
                        for module in dau.modules:
                            for port in module.ports:
                                if random.randint(0, 100) * .01 <= port_modification_chance:
                                    port.add_measurement_to_beginning()

                    elif transformation == PortTransformation.AddMeasurementsArbitrarily:
                        for module in dau.modules:
                            for port in module.ports:
                                if random.randint(0, 100) * .01 <= port_modification_chance:
                                    port.add_measurement_arbitrarily()

                    elif transformation == PortTransformation.AddMeasurementsToEnd:
                        for module in dau.modules:
                            for port in module.ports:
                                if random.randint(0, 100) * .01 <= port_modification_chance:
                                    port.add_measurement_to_end()

                    elif transformation == PortTransformation.ChangePortType:
                        for module in dau.modules:
                            for port in module.ports:
                                if random.randint(0, 100) * .01 <= port_modification_chance:
                                    port.change_port_type()

                    else:
                        raise Exception('Unexpected transformation "' + transformation.name + '"!')

        # if dau_transformation is not None:
        #     inventory = root.xpath("/DAUInventory")[0]
        #     for dau in transformation_daus:
        #         inventory.append(dau.xml)

        _write_xml_to_file(etree.tostring(doc).decode(), self.target_file)
        return rval

    def duplicate_non_matches(self, count: int = 1):
        doc = etree.parse(self.inventory_file)
        root = doc.getroot()
        dau_inventory = root.xpath("/DAUInventory")[0]
        xpath = "./NetworkNode[@ID!='" + "' or @ID!='".join(self.matching_daus) + "']"
        non_matching_daus = dau_inventory.xpath(xpath)

        for counter in range(count):
            for network_node in non_matching_daus:  # type: Element
                dau = Dau(network_node).clone()
                dau_inventory.append(dau.xml)

        _write_xml_to_file(etree.tostring(doc).decode(), self.target_file)

    def duplicate_matches(self, count: int = 1) -> List[str]:
        rval = list()
        doc = etree.parse(self.inventory_file)
        root = doc.getroot()
        dau_inventory = root.xpath("/DAUInventory")[0]
        xpath = "./NetworkNode[@ID='" + "' or @ID='".join(self.matching_daus) + "']"
        matching_daus = dau_inventory.xpath(xpath)

        for counter in range(count):
            for network_node in matching_daus:  # type: Element
                dau = Dau(network_node).clone()
                rval.append(dau.initial_id)
                dau_inventory.append(dau.xml)

        _write_xml_to_file(etree.tostring(doc).decode(), self.target_file)
        return rval


class InventoryExpanderIterator:
    def __init__(self, inventory_file: str, matching_daus: List[str], target_file: str):
        self.matching_daus = matching_daus
        self.target_file = target_file
        shutil.copy(inventory_file, ITERATIVE_TMP_FILE_PATH)
        self.inventory_file = ITERATIVE_TMP_FILE_PATH

    def transform_matching_dau(self, dau_transformation: DauTransformation = None,
                               port_transformations: List[PortTransformation] = None,
                               port_modification_chance: int = 1) -> List[str]:
        ie = InventoryExpander(self.inventory_file, self.matching_daus, self.target_file)
        rval = ie.transform_matching_dau(dau_transformation, port_transformations, port_modification_chance)
        shutil.copy(self.target_file, self.inventory_file)
        return rval

    def add_matching_dau_variation(self, dau_transformation: DauTransformation = None,
                                   port_transformations: List[PortTransformation] = None,
                                   port_modification_chance: int = 1) -> List[str]:
        ie = InventoryExpander(self.inventory_file, self.matching_daus, self.target_file)
        rval = ie.add_matching_dau_variation(dau_transformation, port_transformations, port_modification_chance)
        shutil.copy(self.target_file, self.inventory_file)
        return rval

    def duplicate_non_matches(self, count: int):
        ie = InventoryExpander(self.inventory_file, self.matching_daus, self.target_file)
        ie.duplicate_non_matches(count)
        shutil.copy(self.target_file, self.inventory_file)

    def duplicate_matches(self, count: int) -> List[str]:
        ie = InventoryExpander(self.inventory_file, self.matching_daus, self.target_file)
        rval = ie.duplicate_matches(count)
        shutil.copy(self.target_file, self.inventory_file)
        return rval


class TestScenarioInventoryExpander:
    _scenarios = get_scenarios()

    def __init__(self, scenario_name: str, tag: str):
        self.test_scenario = TestScenarioInventoryExpander._scenarios[scenario_name]
        self.label = self.test_scenario.shortName + '-' + tag
        self.target_path = os.path.join(SCENARIO_XML_TARGET_DIR, self.label + '.xml')
        self.expected_dau_selections = list(self.test_scenario.expectedDauSelections[0])
        self.iei = InventoryExpanderIterator(
            self.test_scenario.xmlInventoryPath,
            self.expected_dau_selections,
            self.target_path
        )

    def transform_matching_dau(self, dau_transformation: DauTransformation = None,
                               port_transformations: List[PortTransformation] = None,
                               port_modification_chance: int = 1) -> 'TestScenarioInventoryExpander':
        self.expected_dau_selections.extend(
            self.iei.transform_matching_dau(dau_transformation, port_transformations, port_modification_chance)
        )
        return self

    def add_matching_dau_variation(self, dau_transformation: DauTransformation = None,
                                   port_transformations: List[PortTransformation] = None,
                                   port_modification_chance: int = 1) -> 'TestScenarioInventoryExpander':
        self.expected_dau_selections.extend(
            self.iei.add_matching_dau_variation(dau_transformation, port_transformations, port_modification_chance)
        )
        return self

    def duplicate_non_matches(self, count: int) -> 'TestScenarioInventoryExpander':
        self.iei.duplicate_non_matches(count)
        return self

    def duplicate_matches(self, count: int) -> 'TestScenarioInventoryExpander':
        self.expected_dau_selections.extend(self.iei.duplicate_matches(count))
        return self

    def get_created_test_scenario(self) -> TestScenario:
        return TestScenario(
            xmlInventoryPath=self.target_path,
            xmlMdlrootInputPath=_unresolve_file(self.test_scenario.xmlMdlrootInputPath),
            shortName=self.label,
            prettyName="Generated Scenario: " + self.label,
            timeoutMS=120000,
            ingestedXmlInventoryHash='null',
            ingestedXmlMdlrootInputHash=self.test_scenario.ingestedXmlMdlrootInputHash,
            expectedStatusSequence=list(self.test_scenario.expectedStatusSequence),
            scenarioType="Scenario5bbn"
        )


def print_metrics(inventory_file: str, matching_daus: List[str]):
    doc = etree.parse(inventory_file)
    root = doc.getroot()
    dau_inventory = root.xpath("/DAUInventory")[0]
    print(
        "Matches: " +
        str(len(dau_inventory.xpath("./NetworkNode[@ID='" + "' or @ID='".join(matching_daus) + "']"))) +
        ", Non-Matches: " +
        str(len(dau_inventory.xpath("./NetworkNode[@ID!='" + "' or @ID!='".join(matching_daus) + "']")))
    )


def duplicate_matching_perturbed_daus(scenario_name: str, count: int,
                                      port_transformations: List[PortTransformation]) -> TestScenario:

    amb_count = 0
    ama_count = 0
    ame_count = 0
    cpt_count = 0
    for transformation in port_transformations:
        if transformation == PortTransformation.AddMeasurementsToBeginning:
            amb_count = amb_count + 1
        elif transformation == PortTransformation.AddMeasurementsArbitrarily:
            ama_count = ama_count + 1
        elif transformation == PortTransformation.AddMeasurementsToEnd:
            ame_count = ame_count + 1
        elif transformation == PortTransformation.ChangePortType:
            cpt_count = 1
        else:
            raise Exception('Bad transformation "' + transformation.name + '"!')

    label = ('duplicate-matching-perturbed-daus-x' + str(count) + '-cpt' + str(cpt_count) +
             '-amb' + str(amb_count) + '-ama' + str(ama_count) + '-ame' + str(ame_count))




    tsie = TestScenarioInventoryExpander(scenario_name, label)
    tsie.duplicate_matches(count)
    tsie.add_matching_dau_variation(None, port_transformations)
    return tsie.get_created_test_scenario()


def gen_space(scenario_name: str, counts: List[int], perturbations: List[List[PortTransformation]]) -> List[
    TestScenario]:
    rval = list()
    for count in counts:
        for perturbation in perturbations:
            rval.append(duplicate_matching_perturbed_daus(scenario_name, count, perturbation))

    return rval


def main():
    generated_scenarios = list()
    # generated_scenarios.append(
    #     TestScenarioInventoryExpander('s5e03i01', '128_nonmatching_daus').duplicate_non_matches(30)
    #         .get_created_test_scenario())

    #     # 1 -> 2 DAUS along modules
    generated_scenarios.append(TestScenarioInventoryExpander('s5e03i01', 'two-dau-along-modules-adaptation')
                               .transform_matching_dau(
        DauTransformation.DauSplitInTwoByModules).get_created_test_scenario())

    # 1 -> 2 DAUS split modules
    generated_scenarios.append(
        TestScenarioInventoryExpander('s5e03i01', 'two-dau-split-modules-adaptation')
            .transform_matching_dau(DauTransformation.DauSplitInTwoMixedModules).get_created_test_scenario())

    # generated_scenarios.append(
    #     TestScenarioInventoryExpander('s5e03i01', 'duplicate_matching_dau')
    #         .add_matching_dau_variation().get_created_test_scenario())

    generated_scenarios.extend(gen_space(
        's5e03i01',
        [2, 4, 8],
        # [10, 20, 30, 40, 50],
        [
            [
                PortTransformation.AddMeasurementsArbitrarily,
            ],
            [
                PortTransformation.AddMeasurementsArbitrarily,
                PortTransformation.AddMeasurementsArbitrarily,
            ],
            [
                PortTransformation.AddMeasurementsArbitrarily,
                PortTransformation.AddMeasurementsArbitrarily,
                PortTransformation.AddMeasurementsArbitrarily,
            ],
            # [
            #     PortTransformation.AddMeasurementsArbitrarily,
            #     PortTransformation.AddMeasurementsArbitrarily,
            #     PortTransformation.AddMeasurementsArbitrarily,
            #     PortTransformation.AddMeasurementsArbitrarily,
            # ],
            # [
            #     PortTransformation.AddMeasurementsArbitrarily,
            #     PortTransformation.AddMeasurementsArbitrarily,
            #     PortTransformation.AddMeasurementsArbitrarily,
            #     PortTransformation.AddMeasurementsArbitrarily,
            #     PortTransformation.AddMeasurementsArbitrarily,
            # ],
            # [
            #     PortTransformation.AddMeasurementsArbitrarily,
            #     PortTransformation.AddMeasurementsArbitrarily,
            #     PortTransformation.AddMeasurementsArbitrarily,
            #     PortTransformation.AddMeasurementsArbitrarily,
            #     PortTransformation.AddMeasurementsArbitrarily,
            #     PortTransformation.AddMeasurementsArbitrarily,
            # ],
            [
                PortTransformation.AddMeasurementsToEnd,
            ],
            [
                PortTransformation.AddMeasurementsToEnd,
                PortTransformation.AddMeasurementsToEnd,
            ],
            [
                PortTransformation.AddMeasurementsToEnd,
                PortTransformation.AddMeasurementsToEnd,
                PortTransformation.AddMeasurementsToEnd,
            ],
            # [
            #     PortTransformation.AddMeasurementsToEnd,
            #     PortTransformation.AddMeasurementsToEnd,
            #     PortTransformation.AddMeasurementsToEnd,
            #     PortTransformation.AddMeasurementsToEnd,
            # ],
            # [
            #     PortTransformation.AddMeasurementsToEnd,
            #     PortTransformation.AddMeasurementsToEnd,
            #     PortTransformation.AddMeasurementsToEnd,
            #     PortTransformation.AddMeasurementsToEnd,
            #     PortTransformation.AddMeasurementsToEnd,
            # ],
            # [
            #     PortTransformation.AddMeasurementsToEnd,
            #     PortTransformation.AddMeasurementsToEnd,
            #     PortTransformation.AddMeasurementsToEnd,
            #     PortTransformation.AddMeasurementsToEnd,
            #     PortTransformation.AddMeasurementsToEnd,
            #     PortTransformation.AddMeasurementsToEnd,
            # ],
            [
                PortTransformation.ChangePortType
            ]
        ]
    ))

    if os.path.exists(SCENARIO_XML_LISTING_FILE):
        scenario_listings_root = json.load(open(SCENARIO_XML_LISTING_FILE))
        scenario_list = scenario_listings_root['scenarios']
    else:
        scenario_list = list()
        scenario_listings_root = {
            'scenarios': scenario_list
        }

    for test_scenario in generated_scenarios:
        scenario_list.append(test_scenario.to_dict())

    json.dump(scenario_listings_root, open(SCENARIO_XML_LISTING_FILE, 'w'), indent=4)

    # target_file = duplicate_matching_perturbed_daus(
    #     's5e03i01',
    #     100,
    #     [
    #         PortTransformation.AddMeasurementsArbitrarily,
    #         PortTransformation.AddMeasurementsArbitrarily,
    #         PortTransformation.AddMeasurementsArbitrarily,
    #         PortTransformation.AddMeasurementsArbitrarily,
    #         PortTransformation.AddMeasurementsArbitrarily,
    #         PortTransformation.AddMeasurementsArbitrarily,
    #     ]
    # )


if __name__ == '__main__':
    main()
