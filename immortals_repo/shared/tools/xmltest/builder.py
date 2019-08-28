import os

from lxml import etree

from xmltest import config
from xmltest.fragments import XmlElement

# BASIC_HEADER = """
BASIC_HEADER = """<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="xs3p.xsl"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
xmlns="http://darpa.mil/immortals/test/{test_root_id}Root"
targetNamespace="http://darpa.mil/immortals/test/{test_root_id}Root"
elementFormDefault="qualified">
"""

BASIC_FOOTER = """</xsd:schema>"""

BASIC_BODY_HEADER = """
<xsd:element name="{test_root_id}" type="{test_root_id}Type"/>
<xsd:complexType name="{test_root_id}Type">
<xsd:annotation>
<xsd:documentation>{test_description}</xsd:documentation>
</xsd:annotation>
<xsd:sequence>
"""

BASIC_BODY_FOOTER = """</xsd:sequence></xsd:complexType>"""

BASIC_XML_HEADER = """
<{test_root_id} xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xmlns="http://darpa.mil/immortals/test/{test_root_id}Root"
xsi:schemaLocation="http://darpa.mil/immortals/test/{test_root_id}Root ../initial.xsd">
"""

BASIC_XML_FOOTER = """\n</{test_root_id}>"""


def _write_xml_to_file(xml_str: str, filepath: str):
    # Redundant, but helps with debugging
    with open(filepath, 'w') as target:
        target.write(xml_str)

    parser = etree.XMLParser(remove_blank_text=True)
    tree = etree.fromstring(xml_str.encode(), parser)
    tree.getroottree().write(filepath, pretty_print=True)


class BasicBuilder:
    def __init__(self, element: XmlElement, target_directory: str):
        self.element = element
        self.target_directory = target_directory

        parent_dir = os.path.abspath(os.path.join(target_directory, '../'))
        if not os.path.exists(parent_dir):
            raise Exception('Parent directory "' + parent_dir + '" does not exist!')

        if not os.path.exists(target_directory):
            os.mkdir(target_directory)

    def build(self):
        d = {
            'test_root_id': self.element.name,
            'test_description': self.element.description
        }

        target_root = os.path.join(config.build_path, self.element.name)
        if not os.path.exists(target_root):
            os.mkdir(target_root)

        initial_xsd = BASIC_HEADER.format(**d) + BASIC_BODY_HEADER.format(**d)
        updated_xsd = BASIC_HEADER.format(**d) + BASIC_BODY_HEADER.format(**d)

        initial_xsd = initial_xsd + self.element.xsd_initial_usage
        updated_xsd = updated_xsd + self.element.xsd_updated_usage

        initial_xsd = initial_xsd + BASIC_BODY_FOOTER.format(**d)
        updated_xsd = updated_xsd + BASIC_BODY_FOOTER.format(**d)

        initial_xsd = initial_xsd + self.element.xsd_initial_declaration
        updated_xsd = updated_xsd + self.element.xsd_updated_declaration

        initial_xsd = initial_xsd + BASIC_FOOTER.format(**d)
        updated_xsd = updated_xsd + BASIC_FOOTER.format(**d)

        _write_xml_to_file(initial_xsd, self.element.initial_xsd_path)
        _write_xml_to_file(updated_xsd, self.element.updated_xsd_path)

        for xml_label in self.element.xml_initial_map.keys():
            xml_body = self.element.xml_initial_map[xml_label]
            initial_xml = BASIC_XML_HEADER.format(**d)
            initial_xml = initial_xml + xml_body
            initial_xml = initial_xml + BASIC_XML_FOOTER.format(**d)
            _write_xml_to_file(initial_xml, self.element.get_initial_xml_path(xml_label))

    def validate(self):
        schema = etree.XMLSchema(etree.parse(self.element.initial_xsd_path))
        for xml_key in self.element.xml_initial_map.keys():
            doc_path = self.element.get_initial_xml_path(xml_key)
            doc = etree.parse(doc_path)

            if not schema.validate(doc):
                err = 'XML file "' + doc_path + '" did not validate against "' + self.element.initial_xsd_path + '"!'
                schema.assertValid(doc)
                raise Exception(err)

            print('File "' + self.element.name + '/xml/' + doc_path.split('/')[-1] + '" validated successfully.')
